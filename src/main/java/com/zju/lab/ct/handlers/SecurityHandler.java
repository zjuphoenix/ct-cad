package com.zju.lab.ct.handlers;

import com.google.inject.Inject;
import com.zju.lab.ct.annotations.RouteHandler;
import com.zju.lab.ct.annotations.RouteMapping;
import com.zju.lab.ct.annotations.RouteMethod;
import com.zju.lab.ct.dao.UserDao;
import com.zju.lab.ct.model.HttpCode;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RouteHandler("/api")
public class SecurityHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityHandler.class);

    @Inject
    private UserDao userDao;
    /**
     * /api/permission
     * POST
     * 获取用户所有权限
     * @return
     */
    @RouteMapping(value = "/permission", method = RouteMethod.POST)
    public Handler<RoutingContext> permissions() {
        return ctx -> {
            User user = ctx.user();
            if (user == null) {
                LOGGER.error("Error, no user");
                ctx.fail(401);
            }
            else {
                try {
                    List<String> permissions = userDao.getUserPermissions(user.principal().getString("username"));
                    JsonArray perms = new JsonArray();
                    permissions.forEach(permission -> {
                        perms.add(permission);
                    });
                    ctx.response().end(perms.encode());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    ctx.response().setStatusCode(HttpCode.INTERNAL_SERVER_ERROR.getCode()).end(e.getMessage());
                }
            }
        };
    }

}
