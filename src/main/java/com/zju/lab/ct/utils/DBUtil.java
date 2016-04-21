package com.zju.lab.ct.utils;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import java.sql.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuhaitao on 2016/4/18.
 */
public class DBUtil {
    public static List<JsonObject> queryRecords(int pageIndex, int pageSize, String username) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:db/cad");
        StringBuilder sb = new StringBuilder("select * from record ");
        if (!StringUtils.isEmpty(username)) {
            sb.append("where username = ").append(username).append(" ");
        }
        sb.append("limit ").append(pageSize).append(" offset ").append((pageIndex-1)*pageSize);
        String sql = sb.toString();
        /*String sql = null;
        if (StringUtils.isEmpty(username)) {
            sql = "select * from record limit ? offset ?";
        }
        else{
            sql = "select * from record where username = ? limit ? offset ?";
        }
        PreparedStatement statement = c.prepareStatement(sql);
        if (StringUtils.isEmpty(username)) {
            statement.setInt(1, pageSize);
            statement.setInt(2, (pageIndex-1)*pageSize);
        }
        else{
            statement.setString(1, username);
            statement.setInt(2, pageSize);
            statement.setInt(3, (pageIndex-1)*pageSize);
        }
        ResultSet rs = statement.executeQuery(sql);*/
        Statement statement = c.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<JsonObject> result = new ArrayList<>(pageSize);
        while (rs.next()) {
            JsonObject obj = new JsonObject();
            obj.put("id", rs.getInt("id"));
            obj.put("diagnosis", rs.getString("diagnosis"));
            obj.put("username", rs.getString("username"));
            result.add(obj);
        }
        rs.close();
        statement.close();
        c.close();
        return result;
    }

    public static int getRecordCount(String username) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:db/cad");
        StringBuilder sb = new StringBuilder("select count(*) from record");
        if (!StringUtils.isEmpty(username)) {
            sb.append(" where username = ").append(username);
        }
        /*String sql = null;
        if (StringUtils.isEmpty(username)) {
            sql = "select count(*) from record";
        }
        else{
            sql = "select count(*) from record where username = ?";
        }*/
        String sql = sb.toString();
        /*PreparedStatement statement = c.prepareStatement(sql);*/
        /*if (!StringUtils.isEmpty(username)) {
            statement.setString(1, username);
        }*/
        Statement statement = c.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        int count = rs.getInt("count(*)");
        rs.close();
        statement.close();
        c.close();
        return count;
    }
}
