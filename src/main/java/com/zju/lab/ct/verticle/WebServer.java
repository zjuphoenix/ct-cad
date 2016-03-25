package com.zju.lab.ct.verticle;

import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.security.APIInterceptorHandler;
import com.zju.lab.ct.security.FormLoginHandlerImpl;
import com.zju.lab.ct.utils.AppUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by wuhaitao on 2015/12/10.
 */
public class WebServer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);

    // Scan handlers from package 'com.zju.lab.ct.handlers.*'
    private static final Reflections handlerReflections = new Reflections("com.zju.lab.ct.handlers");
    // Scan daos from package 'com.zju.lab.ct.dao.*'
    private static final Reflections daoReflections = new Reflections("com.zju.lab.ct.dao");

    private Integer port = AppUtil.configInt("web.server.port");

    protected Router router;

    private Map<Class<?>,Object> daoMap;

    @Override
    public void start() throws Exception {

        LOGGER.info("Start server at port {} .....", port);

        daoMap = new HashMap<>();
        daoMap.put(vertx.getClass(), vertx);

        LOGGER.info("Start scanning daos....");
        Set<Class<?>> daos = daoReflections.getTypesAnnotatedWith(HandlerDao.class);
        for (Class<?> dao : daos) {
            LOGGER.info("Scan dao {}", dao.getSimpleName());
            Constructor constructor = dao.getConstructor(Vertx.class);
            if (constructor == null){
                LOGGER.error("{} is not satisfiable, dao must have a constructor with param type Vertx.class!",dao.getSimpleName());
                continue;
            }
            daoMap.put(dao, constructor.newInstance(vertx));
        }
        LOGGER.info("Scanning dao finished!");

        router = Router.router(vertx);

        /*Cross Origin Resource Sharing*/
        router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST).allowedMethod(HttpMethod.PUT).allowedMethod(HttpMethod.DELETE));

        router.route().handler(CookieHandler.create());
        router.route().handler(BodyHandler.create().setUploadsDirectory(AppUtil.getUploadDir()));
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        AuthProvider authProvider = JDBCAuth.create(AppUtil.getJdbcClient(vertx));
        router.route().handler(UserSessionHandler.create(authProvider));

        router.route("/api/*").handler(new APIInterceptorHandler(authProvider));
        router.route("/upload").handler(new APIInterceptorHandler(authProvider));
        router.route("/uploadFiles").handler(new APIInterceptorHandler(authProvider));

        // registerHandlers
        registerHandlers();

        // Handles the actual login
        router.route("/login").handler(new FormLoginHandlerImpl(authProvider));
        // Implement logout
        router.route("/logout").handler(context -> {
            context.clearUser();
            // Redirect back to the index page
            context.response().putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60").putHeader("location", "/").setStatusCode(302).end();
        });

        // Must be the latest handler to register
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router::accept).listen(port);

    }

    private void registerHandlers() {
        LOGGER.info("Register available request handlers...");
        Set<Class<?>> handlers = handlerReflections.getTypesAnnotatedWith(RouteHandler.class);
        for (Class<?> handler : handlers) {
            try {
                registerNewHandler(handler);
            } catch (Exception e) {
                LOGGER.error("Error register {}", handler);
            }
        }
        LOGGER.info("Register request handlers finished!");
    }

    private void registerNewHandler(Class<?> handler) throws Exception {
        String root = "";
        if (handler.isAnnotationPresent(RouteHandler.class)) {
            RouteHandler routeHandler = handler.getAnnotation(RouteHandler.class);
            root = routeHandler.value();
        }
        LOGGER.info("Handler:{}", handler.getSimpleName());
        Constructor[] constructors = handler.getConstructors();
        Class<?>[] clazzs = constructors[0].getParameterTypes();
        int length = clazzs.length;
        Object[] objects = new Object[length];
        for (int i = 0; i < length; i++) {
            objects[i] = daoMap.get(clazzs[i]);
        }
        Object instance = constructors[0].newInstance(objects);
        /*for (Constructor constructor:constructors){
            Class<?>[] clazzs = constructor.getParameterTypes();
            int length = clazzs.length;
            Object[] objects = new Object[length];
            for (int i = 0; i < length; i++) {
                objects[i] = daoMap.get(clazzs[i]);
            }
            instance = constructor.newInstance(objects);
            break;
        }*/
        Method[] methods = handler.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(RouteMapping.class)) {
                RouteMapping mapping = method.getAnnotation(RouteMapping.class);
                RouteMethod routeMethod = mapping.method();
                String url = root + mapping.value();
                Handler<RoutingContext> methodHandler = (Handler<RoutingContext>) method.invoke(instance);
                LOGGER.info("Register New Handler -> {}:{}", routeMethod, url);
                switch (routeMethod) {
                    case POST:
                        router.post(url).handler(methodHandler);
                        break;
                    case PUT:
                        router.put(url).handler(methodHandler);
                        break;
                    case DELETE:
                        router.delete(url).handler(methodHandler);
                        break;
                    case GET: // fall through
                    default:
                        router.get(url).handler(methodHandler);
                        break;
                }
            }
        }
    }

}
