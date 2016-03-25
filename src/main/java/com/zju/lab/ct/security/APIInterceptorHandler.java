package com.zju.lab.ct.security;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIInterceptorHandler extends AuthHandlerImpl {
    private static Logger LOGGER = LoggerFactory.getLogger(APIInterceptorHandler.class);

    public APIInterceptorHandler(AuthProvider authProvider) {
        super(authProvider);
    }

    /**
     * 用户验证拦截器
     * @param context
     */
    @Override
    public void handle(RoutingContext context) {
        Session session = context.session();
        HttpServerResponse response = context.response();
        response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
        if (session != null) {
            User user = context.user();
            if (user != null) {
                LOGGER.info("authorise user: {}", user.principal().encode());
                authorise(user, context);
            } else {
                LOGGER.info("user is not authorised!");
                response.setStatusCode(401).end(); // Unauthorized
            }
        } else {
            LOGGER.error("No session!");
            context.fail(new NullPointerException("No session.."));
        }
    }
}
