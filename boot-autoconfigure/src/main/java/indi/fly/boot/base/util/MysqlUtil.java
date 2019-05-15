package indi.fly.boot.base.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.sql.*;
import java.util.List;

class MysqlUtil {

    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/mysql?characterEncoding=utf8&autoReconnect=true&useSSL=false";
    private volatile static Connection con = null;

    private static Connection getConnection() {
        if (con == null) {
            synchronized (MysqlUtil.class){
                if (con == null) {
                    try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return con;
    }


    /**
     * @desc 增删改
     * @author xiezhenxiang 2019/5/14
     * @param sql sql语句
     * @param params 参数
     **/
    public static boolean executeUpdate(String sql, List<Object> params)
            throws SQLException {
        Connection conn = getConnection();
        PreparedStatement statement;
        statement = conn.prepareStatement(sql);
        if (params != null && !params.isEmpty()) {
            int index = 1;
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(index ++, i);
            }
        }
        // 影响数据库的行数
        int result = statement.executeUpdate();
        return result > 0;
    }

    /**
     * 从数据库中查询数据
     */
    public static JSONArray executeQuery(String sql, List<Object> params){
        Connection conn = getConnection();
        JSONArray arr = new JSONArray();
        PreparedStatement statement;
        try {
            int index = 1;
            statement = conn.prepareStatement(sql);
            if (params != null && !params.isEmpty()) {
                for (Object para : params) {
                    statement.setObject(index ++, para);
                }
            }
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int cols_len = metaData.getColumnCount();
            while (resultSet.next()) {
                JSONObject obj = new JSONObject();
                for (int i = 0; i < cols_len; i++) {
                    String cols_name = metaData.getColumnName(i + 1);
                    Object cols_value = resultSet.getObject(cols_name);
                    obj.put(cols_name, cols_value);
                }
                arr.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }
}