package com.zju.lab.ct.mapper;

import com.zju.lab.ct.model.UserDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by wuhaitao on 2016/4/19.
 */
public interface UserMapper {
    List<UserDto> queryUsers() throws Exception;
    String queryUserByUsername(@Param("username") String username) throws Exception;
    List<String> queryUserPermission(@Param("username") String username) throws Exception;
    void addUser(@Param("username") String username, @Param("password") String password, @Param("password_salt") String password_salt) throws Exception;
    void addUserRole(@Param("username") String username, @Param("role") String role) throws Exception;
    void deleteUser(@Param("username") String username) throws Exception;
    void deleteUserRole(@Param("username") String username) throws Exception;
}
