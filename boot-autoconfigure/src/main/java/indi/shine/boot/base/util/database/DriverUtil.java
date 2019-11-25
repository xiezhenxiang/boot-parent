package indi.shine.boot.base.util.database;

import com.alibaba.fastjson.JSONObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.assertj.core.util.Lists;

import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * support mysql, hive, dm
 * @author xiezhenxiang 2019/8/1
 **/
public class DriverUtil {

    private HikariDataSource dataSource = null;
    private String url;
    private String userName;
    private String pwd;
    private static Map<String, HikariDataSource> pool = new HashMap<>();

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
        initDataSource();
    }


    /**
     * 增删改
     * @param sql sql语句
     * @param params 参数
     **/
    public boolean update(String sql, Object... params) {

        Connection con = getConnection();
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
        } finally {
            close(con);
        }
        return result > 0;
    }

    /**
     * 查找
     * @param sql sql语句
     * @param params 参数
     **/
    public List<JSONObject> find(String sql, Object... params){

        Connection con = getConnection();
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
        } finally {
            close(con);
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
            String infoMysqlUrl = url.replaceAll("/" + dbName, "/information_schema");
            DriverUtil infoMysqlUtil = getInstance(infoMysqlUrl, userName, pwd);
            String sql = "select TABLE_NAME, TABLE_COMMENT from TABLES where TABLE_SCHEMA = ?";
            ls = infoMysqlUtil.find(sql, dbName).stream().map(s -> s.getString("TABLE_NAME")).collect(Collectors.toList());
        }
        return ls;
    }

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
            update(sql, values.toArray());
        }
        return true;
    }

    public boolean updateSelective(String tbName, JSONObject bean, String... queryField) {

        List<String> queryFieldLs = Lists.newArrayList(queryField);

        String sql = "update " + tbName + " set ";
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : bean.entrySet()) {
            if (!queryFieldLs.contains(entry.getKey()) && entry.getValue() != null) {
                sql += entry.getKey() + " = ? , ";
                values.add(entry.getValue());
            }
        }

        if (!values.isEmpty()) {

            sql = sql.substring(0, sql.length() - 2);
            if (!queryFieldLs.isEmpty()) {
                sql += " where ";
                for (String field : queryFieldLs) {
                    sql += field + " = ? and ";
                    values.add(bean.get(field));
                }

                sql = sql.substring(0, sql.length() - 5);
            }
            return update(sql, values.toArray());
        }

        return true;
    }


    private void initDataSource() {

        if (dataSource == null || dataSource.isClosed()) {

            String key = url;

            if (pool.containsKey(key)) {
                dataSource = pool.get(key);
                if (dataSource == null || dataSource.isClosed()) {
                    pool.remove(key);
                } else {
                    return;
                }
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(userName);
            config.setPassword(pwd);

            String driveClassName = "com.mysql.jdbc.Driver";
            if (url.contains("jdbc:dm:")) {
                driveClassName = "dm.jdbc.driver.DmDriver";
            } else if (url.contains("jdbc:hive")) {
                driveClassName = "org.apache.hive.jdbc.HiveDriver";
            }
            config.setDriverClassName(driveClassName);
            config.setConnectionTestQuery("SELECT 1");
            config.setMinimumIdle(10);
            config.setMaximumPoolSize(20);
            config.setConnectionTimeout(3000);
            config.setValidationTimeout(5000);
            config.setMaxLifetime(MINUTES.toMillis(30));
            config.setIdleTimeout(MINUTES.toMillis(10));
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            pool.put(key, dataSource);
        }
    }

    public Connection getConnection() {

        initDataSource();
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("get connection error");
        }
    }

    private void close(Connection con) {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("driver connection close error");
        }
    }

    public static void main(String[] args) {

        DriverUtil driverUtil = DriverUtil.getMysqlInstance("192.168.4.11", 3306, "plantdata_manage", "root", "root@hiekn");

        JSONObject obj = new JSONObject();
        obj.put("name", "name");
        obj.put("phone", "123");
        obj.put("age", 18);
        obj.put("date", new Date());
        driverUtil.updateSelective("t_snapshot", obj, "name", "phone");

    }
}