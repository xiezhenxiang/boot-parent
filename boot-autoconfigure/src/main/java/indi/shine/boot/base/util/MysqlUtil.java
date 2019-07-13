package indi.shine.boot.base.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import java.sql.*;
import java.util.List;
import java.util.Map;

class MysqlUtil {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/mysql?serverTimezone=UTC&characterEncoding=utf8&autoReconnect=true&useSSL=false";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private volatile static Connection con = null;

    /**
     * 增删改
     * @author xiezhenxiang 2019/5/14
     * @param sql sql语句
     * @param params 参数
     **/
    public static boolean executeUpdate(String sql, Object... params) {
        getConnection();
        PreparedStatement statement;
        int result = 0;
        try {
            statement = con.prepareStatement(sql);
            int index = 1;
            if (params != null && params.length > 0) {
                for (Object para : params) {
                    statement.setObject(index++, para);
                }
            }
            // update lines num
            result = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result > 0;
    }

    /**
     * 查找
     * @author xiezhenxiang 2019/5/14
     * @param sql sql语句
     * @param params 参数
     **/
    public static List<JSONObject> executeQuery(String sql, Object... params){
        getConnection();
        List<JSONObject> ls = Lists.newArrayList();
        PreparedStatement statement;
        try {
            int index = 1;
            statement = con.prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (Object para : params) {
                    statement.setObject(index ++, para);
                }
            }
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int colsLen = metaData.getColumnCount();
            while (resultSet.next()) {
                JSONObject obj = new JSONObject();
                for (int i = 0; i < colsLen; i++) {
                    String colsName = metaData.getColumnName(i + 1);
                    Object colsValue = resultSet.getObject(colsName);
                    obj.put(colsName, colsValue);
                }
                ls.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ls;
    }

    /**
     * @desc 插入数据
     * @author xiezhenxiang 2019/6/1
     * @param tbName 表名
     * @param bean 数据
     **/
    public static boolean insertSelective(String tbName, JSONObject bean) {
        String sql = "insert into " + tbName + " (";
        List<Object> values = Lists.newArrayList();
        for (Map.Entry<String, Object> entry : bean.entrySet()) {
            if (entry.getValue() != null) {
                sql += entry.getKey() + ", ";
                values.add(entry.getValue());
            }
        }
        if (!values.isEmpty()) {
            sql = sql.substring(0, sql.length() - 2) + ") values (";
            for (int i = 0; i < values.size(); i ++) {
                sql += "?, ";
            }
            sql = sql.substring(0, sql.length() - 2) + ")";
        } else {
            return true;
        }
        executeUpdate(sql, values.toArray());
        return true;
    }


    private static Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                synchronized (MysqlUtil.class) {
                    if (con == null || con.isClosed()) {
                        try {
                            Class.forName("com.mysql.cj.jdbc.Driver");
                            con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
}