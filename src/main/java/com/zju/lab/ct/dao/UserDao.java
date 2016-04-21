package com.zju.lab.ct.dao;

import com.google.inject.Inject;
import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.mapper.UserMapper;
import com.zju.lab.ct.model.HttpCode;
import com.zju.lab.ct.model.ResponseMsg;
import com.zju.lab.ct.model.User;
import com.zju.lab.ct.model.UserDto;
import com.zju.lab.ct.utils.AppUtil;
import io.vertx.core.Handler;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
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

    private SqlSessionFactory sqlSessionFactory;

    @Inject
    public UserDao(SqlSessionFactory sqlSessionFactory) throws UnsupportedEncodingException {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 获取所有用户
     * @param done
     */
    public void getUsers(Handler<ResponseMsg<List<UserDto>>> done){
        SqlSession session = sqlSessionFactory.openSession();
        UserMapper userMapper = session.getMapper(UserMapper.class);
        try {
            List<UserDto> userDtos = userMapper.queryUsers();
            ResponseMsg<List<UserDto>> users = new ResponseMsg(userDtos);
            done.handle(users);
        } catch (Exception e) {
            done.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    /**
     * 添加用户
     * @param user
     * @param done
     */
    public void addUser(User user, Handler<ResponseMsg<String>> done){
        String username = user.getUsername();
        String password = user.getPassword();
        String role = user.getRole();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            LOGGER.error("Username and Password cannot be null");
            done.handle(new ResponseMsg<>(HttpCode.BAD_REQUEST, "Username and Password cannot be null"));
        }
        String salt = AppUtil.computeHash(username, null, "SHA-512");
        String passwordHash = AppUtil.computeHash(password, salt, "SHA-512");
        SqlSession session = sqlSessionFactory.openSession(false);
        UserMapper userMapper = session.getMapper(UserMapper.class);
        try {
            String uname = userMapper.queryUserByUsername(username);
            if (uname!=null){
                done.handle(new ResponseMsg<>(HttpCode.BAD_REQUEST, "username has already registered"));
            }
            else{
                userMapper.addUser(username, passwordHash, salt);
                userMapper.addUserRole(username, role);
                done.handle(new ResponseMsg<>("add user success"));
            }
            session.commit();
        } catch (Exception e) {
            done.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    /**
     * 删除用户
     * @param username
     * @param done
     */
    public void deleteUser(String username, Handler<ResponseMsg<String>> done){
        if (StringUtils.isEmpty(username)){
            done.handle(new ResponseMsg<>(HttpCode.BAD_REQUEST, "username must not be null or empty"));
        }
        else{
            SqlSession session = sqlSessionFactory.openSession(false);
            UserMapper userMapper = session.getMapper(UserMapper.class);
            try {
                userMapper.deleteUser(username);
                userMapper.deleteUserRole(username);
                session.commit();
                done.handle(new ResponseMsg<>("delete user success"));
            } catch (Exception e) {
                done.handle(new ResponseMsg<>(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage()));
            }
        }
    }

    /**
     * 获取用户权限列表
     * @param username
     * @return
     * @throws Exception
     */
    public List<String> getUserPermissions(String username) throws Exception {
        SqlSession session = sqlSessionFactory.openSession();
        UserMapper userMapper = session.getMapper(UserMapper.class);
        return userMapper.queryUserPermission(username);
    }
}
