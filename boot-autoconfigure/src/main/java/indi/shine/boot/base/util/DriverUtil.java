package indi.shine.boot.base.util;

import com.alibaba.fastjson.JSONObject;
import indi.shine.boot.base.exception.ServiceException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static indi.shine.boot.base.util.AlgorithmUtil.elfHash;

/**
 * support mysql, hive, dm
 * @author xiezhenxiang 2019/8/1
 **/
public class DriverUtil {

    private String url;
    private String userName;
    private String pwd;
    private volatile Connection con;
    private volatile static Map<Integer, Connection> pool = new HashMap<>(10);

    public static DriverUtil getInstance(String url, String userName, String pwd ) {

        return new DriverUtil(url, userName, pwd);
    }

    public static DriverUtil getMysqlInstance(String ip, Integer port, String database, String userName, String pwd) {

        String url = "jdbc:mysql://" + ip + ":" + port + "/"  + database
                + "?serverTimezone=UTC&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&useSSL=false";
        return new DriverUtil(url, userName, pwd);
    }

    private DriverUtil(String url, String userName, String pwd) {

        this.url = url;
        this.userName = userName;
        this.pwd = pwd;
        initConnection();
    }


    /**
     * 增删改
     * @author xiezhenxiang 2019/5/14
     * @param sql sql语句
     * @param params 参数
     **/
    public boolean update(String sql, Object... params) {

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
    public List<JSONObject> find(String sql, Object... params){

        List<JSONObject> ls = new ArrayList<>();
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

    public JSONObject findOne(String sql, Object... params){

        List<JSONObject> ls = find(sql, params);
        return ls.isEmpty() ? new JSONObject() : ls.get(0);
    }

    public List<String> getTables() {

        List<String> ls;
        if (url.contains("jdbc:hive2")) {
            ls = find("show tables").stream().map(s -> s.getString("tab_name")).collect(Collectors.toList());
        } else {
            String dbName =url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
            String infoMysqlUrl = url.replaceAll(dbName, "information_schema");
            String sql = "select TABLE_NAME, TABLE_COMMENT from TABLES where TABLE_SCHEMA = ?";
            ls = find(sql, dbName).stream().map(s -> s.getString("TABLE_NAME")).collect(Collectors.toList());
        }
        return ls;
    }

    /**
     * 插入数据
     * @author xiezhenxiang 2019/6/1
     **/
    public boolean insertSelective(String tbName, JSONObject bean) {

        String sql = "insert into " + tbName + " (";
        List<Object> values = new ArrayList<>();
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
        update(sql, values.toArray());
        return true;
    }


    private void initConnection() {

        if (con == null) {

            synchronized (DriverUtil.class) {

                if (con == null) {
                    Integer key = elfHash(url);
                    if (pool.containsKey(key)) {
                        con = pool.get(key);
                    } else {
                        String className = "com.mysql.jdbc.Driver";
                        if (url.contains("jdbc:dm:")) {
                            className = "dm.jdbc.driver.DmDriver";
                        } else if (url.contains("jdbc:hive")) {
                            className = "org.apache.hive.jdbc.HiveDriver";
                        }
                        try {
                            Class.forName(className);
                            con = DriverManager.getConnection(url, userName, pwd);
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw ServiceException.newInstance(50050, "数据库连接失败!");
                        }
                        pool.put(key, con);
                    }
                }
            }
        }
    }

}