package com.zju.lab.ct.dao;

import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.ResponseMsg;
import com.zju.lab.ct.model.User;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.SQLUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by wuhaitao on 2016/3/21.
 */
@HandlerDao
public class UserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    protected JDBCClient sqlite = null;

    public UserDao(Vertx vertx) throws UnsupportedEncodingException {
        JsonObject sqliteConfig = new JsonObject()
                .put("url", AppUtil.configStr("db.url"))
                .put("driver_class", AppUtil.configStr("db.driver_class"));
        sqlite = JDBCClient.createShared(vertx, sqliteConfig, "user");
    }

    /**
     * 获取所有用户
     * @param done
     */
    public void getUsers(Handler<ResponseMsg> done){
        sqlite.getConnection(connection -> {
            if (connection.succeeded()){
                SQLConnection conn = connection.result();
                conn.query("select USER.USERNAME,USER_ROLES.ROLE from USER inner join USER_ROLES on USER.USERNAME = USER_ROLES.USERNAME", res -> {
                    if (res.succeeded()){
                        ResponseMsg<List<JsonObject>> users = new ResponseMsg(res.result().getRows());
                        done.handle(users);
                    }
                    else{
                        done.handle(new ResponseMsg(HttpCode.BAD_REQUEST, res.cause().getMessage()));
                    }
                    SQLUtil.close(conn);
                });
            }
            else{
                done.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
            }
        });
    }

    /**
     * 添加用户
     * @param user
     * @param done
     */
    public void addUser(User user, Handler<ResponseMsg<String>> done){
        sqlite.getConnection(connection -> {
            if (connection.succeeded()){
                String username = user.getUsername();
                String password = user.getPassword();
                String role = user.getRole();
                if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                    LOGGER.error("Username and Password cannot be null");
                    done.handle(new ResponseMsg<String>(HttpCode.BAD_REQUEST, "Username and Password cannot be null"));
                }
                String salt = AppUtil.computeHash(username, null, "SHA-512");
                String passwordHash = AppUtil.computeHash(password, salt, "SHA-512");
                SQLConnection conn = connection.result();
                conn.queryWithParams("select * from USER where USERNAME = ?", new JsonArray().add(username), resultSetAsyncResult -> {
                    if (resultSetAsyncResult.succeeded()){
                        if (resultSetAsyncResult.result().getResults().size() >= 1){
                            done.handle(new ResponseMsg<String>(HttpCode.BAD_REQUEST, "username has already registered"));
                        }
                        else{
                            JsonArray params = new JsonArray();
                            params.add(username).add(passwordHash).add(salt);
                            conn.updateWithParams("insert into USER (USERNAME, PASSWORD, PASSWORD_SALT) values (?, ?, ?)", params, updateResultAsyncResult -> {
                                if (updateResultAsyncResult.succeeded()){
                                    conn.updateWithParams("insert into USER_ROLES (USERNAME, ROLE) values (?, ?)", new JsonArray().add(username).add(role), updateResultAsyncResult1 -> {
                                        if (updateResultAsyncResult1.succeeded()){
                                            done.handle(new ResponseMsg("add user success"));
                                        }
                                        else{
                                            done.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, "USER_ROLE datatable insert exception"));
                                        }
                                        SQLUtil.close(conn);
                                    });

                                }
                                else{
                                    done.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, updateResultAsyncResult.cause().getMessage()));
                                    SQLUtil.close(conn);
                                }
                            });
                        }
                    }
                    else{
                        done.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, resultSetAsyncResult.cause().getMessage()));
                        SQLUtil.close(conn);
                    }
                });

            }
            else{
                done.handle(new ResponseMsg(HttpCode.INTERNAL_SERVER_ERROR, connection.cause().getMessage()));
            }
        });
    }

    /**
     * 删除用户
     * @param username
     * @param done
     */
    public void deleteUser(String username, Handler<ResponseMsg<String>> done){
        if (StringUtils.isEmpty(username)){
            done.handle(new ResponseMsg(HttpCode.BAD_REQUEST, "username must not be null or empty"));
        }
        sqlite.getConnection(sqlConnectionAsyncResult -> {
            if (sqlConnectionAsyncResult.succeeded()){
                SQLConnection sqlConnection = sqlConnectionAsyncResult.result();
                sqlConnection.updateWithParams("delete from USER where USERNAME = ?", new JsonArray().add(username), updateResultAsyncResult -> {
                    if (updateResultAsyncResult.succeeded()){
                        sqlConnection.updateWithParams("delete from USER_ROLES where USERNAME = ?", new JsonArray().add(username), updateResultAsyncResult1 -> {
                            if (updateResultAsyncResult1.succeeded()){
                                done.handle(new ResponseMsg<String>("delete user success"));
                            }
                            else{
                                done.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, updateResultAsyncResult1.cause().getMessage()));
                            }
                            SQLUtil.close(sqlConnection);
                        });
                    }
                    else{
                        done.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, updateResultAsyncResult.cause().getMessage()));
                        SQLUtil.close(sqlConnection);
                    }
                });
            }
            else{
                done.handle(new ResponseMsg<String>(HttpCode.INTERNAL_SERVER_ERROR, sqlConnectionAsyncResult.cause().getMessage()));
            }
        });
    }
}
