package com.zju.lab.ct.orm.container;

import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.orm.MethodMapperInfo;
import com.zju.lab.ct.orm.ORMConfig;
import com.zju.lab.ct.orm.TestMapperImpl;
import com.zju.lab.ct.utils.AppUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Created by wuhaitao on 2016/3/25.
 */
public class MapperScaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapperScaner.class);

    private Map<Class<?>,Object> daoMap;

    public MapperScaner() {
        daoMap = new HashMap<>();
    }

    private Method[] getMethodsByDao(Class<?> dao){
        return dao.getDeclaredMethods();
    }

    private MethodMapperInfo getMethodMapperInfo(Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Annotation methodAnnotation= method.getAnnotations()[0];
        Method m = methodAnnotation.getClass().getDeclaredMethod("value", null);
        String value = (String) m.invoke(methodAnnotation, null);
        Parameter[] parameters = m.getParameters();
        List<String> params = new ArrayList<>();
        for (Parameter parameter : parameters) {
            if (parameter.getType() != Handler.class) {
                params.add(parameter.getName());
            }
        }
        return new MethodMapperInfo(methodAnnotation, value, params);
    }

    public void constructDaos(){
        Reflections daoReflections = new Reflections(ORMConfig.mapperPath);
        Set<Class<?>> daos = daoReflections.getTypesAnnotatedWith(HandlerDao.class);
        for (Class<?> dao : daos) {
            LOGGER.info("Scan dao {}", dao.getSimpleName());
            TestMapperImpl testMapper = new TestMapperImpl();
            Object obj = Proxy.newProxyInstance(testMapper.getClass().getClassLoader(), testMapper.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    LOGGER.info(method.getName());
                    LOGGER.info(""+args.length);
                    LOGGER.info(args[0].getClass().getName());

                    /*JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());
                    Annotation methodAnnotation= method.getAnnotations()[0];
                    Method m = methodAnnotation.getClass().getDeclaredMethod("value", null);
                    String value = (String) m.invoke(methodAnnotation, null);
                    Parameter[] parameters = m.getParameters();
                    List<String> params = new ArrayList<>();
                    for (Parameter parameter : parameters) {
                        params.add(parameter.getName());
                    }
                    Map<String,Object> paramValueMap = new HashMap<String, Object>();
                    Map<String,Class<?>> paramTypeMap = new HashMap<String, Class<?>>();
                    for (int i=0;i<params.size();i++){
                        paramValueMap.put(params.get(i), args[i]);
                        paramTypeMap.put(params.get(i), parameters[i].getType());
                    }
                    JsonArray sqlParams = new JsonArray();
                    String[] sqls = value.split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String s : sqls){
                        if (s.startsWith("#")){
                            String name = s.substring(2,s.length()-1);
                            Class<?> clazz = paramTypeMap.get(name);
                            if (clazz == int.class){
                                sqlParams.add((int)paramValueMap.get(name));
                            }
                            else if(clazz == String.class){
                                sqlParams.add((String)paramValueMap.get(name));
                            }
                            sb.append("?").append(" ");
                        }
                        else{
                            sb.append(s).append(" ");
                        }
                    }
                    sb.deleteCharAt(sb.length()-1);
                    String sql = sb.toString();
                    Object obj = args[args.length-1];
                    Handler<MapperHandlerResponse> handler = (Handler<MapperHandlerResponse>)obj;
                    String methodAnnotationStr = methodAnnotation.toString();
                    client.getConnection(sqlConnectionAsyncResult -> {
                        MapperHandlerResponse response = new MapperHandlerResponse();
                        if (sqlConnectionAsyncResult.succeeded()){
                            SQLConnection sqlConnection = sqlConnectionAsyncResult.result();
                            switch (methodAnnotationStr){
                                case "INSERT":
                                case "UPDATE":
                                case "DELETE":
                                    sqlConnection.updateWithParams(sql, sqlParams, updateResultAsyncResult -> {
                                        if (updateResultAsyncResult.succeeded()){
                                            response.setKeys(updateResultAsyncResult.result().getKeys());
                                            handler.handle(response);
                                        }
                                        else{
                                            response.setCode(HttpCode.INTERNAL_SERVER_ERROR);
                                            response.setError("database update failed");
                                            LOGGER.error("database update failed");
                                            handler.handle(response);
                                        }
                                    });
                                    break;
                                case "SELECT":
                                    sqlConnection.queryWithParams(sql, sqlParams, resultAsyncResult -> {
                                        if (resultAsyncResult.succeeded()){
                                            response.setResult(resultAsyncResult.result().getRows());
                                            handler.handle(response);
                                        }
                                        else{
                                            response.setCode(HttpCode.INTERNAL_SERVER_ERROR);
                                            response.setError("database query failed");
                                            LOGGER.error("database query failed");
                                            handler.handle(response);
                                        }
                                    });
                                    break;
                            }
                        }
                        else{
                            response.setCode(HttpCode.INTERNAL_SERVER_ERROR);
                            response.setError("connect database failed");
                            LOGGER.error("connect database failed");
                            handler.handle(response);
                        }
                    });*/
                    return method.invoke(testMapper,args);
                }
            });
            daoMap.put(dao.getClass(), obj);
        }
    }

    public Object getMapper(Class<?> clazz){
        return daoMap.get(clazz);
    }
}
