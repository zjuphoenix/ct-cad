package com.zju.lab.ct.handlers;

import com.google.inject.Inject;
import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.dao.UserDao;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.User;
import com.zju.lab.ct.model.UserDto;
import com.zju.lab.ct.utils.AppUtil;
import com.zju.lab.ct.utils.RoleMap;
import com.zju.lab.ct.utils.SQLUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RouteHandler("/api/users")
public class  UserHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHandler.class);

    @Inject
    private UserDao userDao;

    /**
     * /api/users
     * GET
     * 获取用户列表handler
     * @return
     */
    @RouteMapping(method = RouteMethod.GET)
    public Handler<RoutingContext> list() {
        return ctx -> {
            LOGGER.debug("Start get list");
            userDao.getUsers(responseMsg -> {
                HttpServerResponse response = ctx.response();
                //response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
                if (responseMsg.getCode().getCode() == HttpCode.OK.getCode()){
                    JsonArray array = new JsonArray();
                    List<UserDto> userDtos = responseMsg.getContent();
                    userDtos.forEach(userDto -> {
                        array.add(new JsonObject().put("USERNAME", userDto.getUSERNAME()).put("ROLE", userDto.getROLE()));
                    });
                    response.setChunked(true).setStatusCode(responseMsg.getCode().getCode()).end(array.encode());
                }
                else{
                    response.setChunked(true).setStatusCode(responseMsg.getCode().getCode()).end(responseMsg.getError());
                }
            });
        };
    }

    /**
     * /api/users
     * POST
     * 添加用户handler
     * @return
     */
    @RouteMapping(method = RouteMethod.POST)
    public Handler<RoutingContext> add() {
        return ctx -> {
            JsonObject user = ctx.getBodyAsJson();
            String username = user.getString("username");
            String password = user.getString("password");
            int role = Integer.parseInt(user.getString("role"));
            String roleStr = "";
            HttpServerResponse response = ctx.response();
            //response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
            try {
                roleStr = RoleMap.getRole(role);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                response.setStatusCode(HttpCode.BAD_REQUEST.getCode()).end(e.getMessage());
                return;
            }

            userDao.addUser(new User(username, password, roleStr), stringResponseMsg -> {
                response.setChunked(true).setStatusCode(stringResponseMsg.getCode().getCode()).end(stringResponseMsg.getContent());
            });
        };
    }

    /**
     * /api/users/:username
     * 获取用户信息handler，目前该功能尚未实现网页操作入口
     * GET
     * @return
     */
    @RouteMapping(value = "/:username", method = RouteMethod.GET)
    public Handler<RoutingContext> edit() {
        return ctx -> {
            String username = ctx.request().getParam("username");
            HttpServerResponse response = ctx.response();
            //response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
            if (StringUtils.isBlank(username)) {
                LOGGER.error("Username is blank");
                ctx.fail(404);
            }

            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());
            client.getConnection(conn -> {
                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(400);
                }
                SQLUtil.query(conn.result(), "select USERNAME from USER where USERNAME = ?", new JsonArray().add(username), res -> {
                    SQLUtil.close(conn.result());
                    if (res.getRows().size() == 1) {
                        response.end(res.getRows().get(0).encode());
                    } else {
                        JsonObject error = new JsonObject();
                        error.put("error", "Record not found");
                        response.setStatusCode(205).end(error.encode());
                    }
                });
            });
        };
    }

    /**
     * /api/users/:username
     * PUT
     * 编辑用户handler
     * @return
     */
    @RouteMapping(value = "/:username", method = RouteMethod.PUT)
    public Handler<RoutingContext> update() {
        return ctx -> {
            JsonObject user = ctx.getBodyAsJson();
            String username = user.getString("username");
            HttpServerResponse response = ctx.response();
            //response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
            if (StringUtils.isBlank(username)) {
                LOGGER.error("Username is blank");
                ctx.fail(404);
            }

            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());
            client.getConnection(conn -> {
                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(404);
                }

                JsonArray params = new JsonArray();
                params.add(username);
                SQLUtil.update(conn.result(), "update USER set FIRST_NAME = ?, LAST_NAME = ?, ADDRESS = ? where USERNAME = ?", params, res -> {
                    SQLUtil.query(conn.result(), "select USERNAME, FIRST_NAME, LAST_NAME, ADDRESS from USER where USERNAME = ?", new JsonArray().add(username), rs -> {
                        SQLUtil.close(conn.result());
                        response.end(rs.getRows().get(0).encode());
                    });
                });
            });
        };
    }

    /**
     * /api/users/:username
     * DELETE
     * 删除用户handler
     * @return
     */
    @RouteMapping(value = "/:username", method = RouteMethod.DELETE)
    public Handler<RoutingContext> delete() {
        return ctx -> {
            String username = ctx.request().getParam("username");
            userDao.deleteUser(username, stringResponseMsg -> {
                HttpServerResponse response = ctx.response();
                //response.putHeader("Access-Control-Allow-Origin", "*").putHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS").putHeader("Access-Control-Max-Age", "60");
                response.setChunked(true).setStatusCode(stringResponseMsg.getCode().getCode()).end(stringResponseMsg.getContent());
            });
        };
    }

}
