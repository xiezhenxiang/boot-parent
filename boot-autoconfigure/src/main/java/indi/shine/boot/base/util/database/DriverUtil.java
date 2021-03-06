package indi.shine.boot.base.util.database;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import indi.shine.boot.base.util.AlgorithmUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private String key;
    private static ConcurrentHashMap<String, HikariDataSource> pool = new ConcurrentHashMap<>();

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
        key = AlgorithmUtil.elfHash(url + userName + pwd).toString();
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
            result = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("jdbc executeUpdate fail");
        } finally {
            close(con);
        }
        return result > 0;
    }

    /**
     * 统计
     * @param sql sql
     **/
    public long count(String sql, Object... params) {
        Map<String, Object> one = findOne(sql, params);
        for (Object v : one.values()) {
            if (v instanceof Integer || v instanceof Long) {
                return Long.parseLong(v.toString());
            }
        }
        return 0;
    }

    /**
     * 查找
     * @param sql sql语句
     * @param params 参数
     **/
    public List<Map<String, Object>> findMany(String sql, Object... params){
        Connection con = getConnection();
        List<Map<String, Object>> ls = new ArrayList<>();
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
                Map<String, Object> obj = new HashMap<>();
                for (int i = 0; i < colsLen; i++) {
                    String colsName = metaData.getColumnName(i + 1);
                    Object colsValue = resultSet.getObject(colsName);
                    obj.put(colsName, colsValue);
                }
                ls.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("jdbc executeQuery fail");
        } finally {
            close(con);
        }
        return ls;
    }

    public Map<String, Object> findOne(String sql, Object... params){
        List<Map<String, Object>> ls = findMany(sql, params);
        return ls.isEmpty() ? null : ls.get(0);
    }

    public boolean insertSelective(String tbName, Map<String, Object> bean) {
        StringBuilder sql = new StringBuilder("insert into `" + tbName + "` (");
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, Object> entry : bean.entrySet()) {
            if (entry.getValue() != null) {
                sql.append(entry.getKey()).append(", ");
                values.add(entry.getValue());
            }
        }
        if (!values.isEmpty()) {
            sql = new StringBuilder(sql.substring(0, sql.length() - 2) + ") values (");
            for (int i = 0; i < values.size(); i ++) {
                sql.append("?, ");
            }
            sql = new StringBuilder(sql.substring(0, sql.length() - 2) + ")");
            update(sql.toString(), values.toArray());
        }
        return true;
    }

    public boolean updateSelective(String tbName, Map<String, Object> bean, String... queryField) {
        List<String> queryFieldLs = Lists.newArrayList(queryField);
        StringBuilder sql = new StringBuilder("update `" + tbName + "` set ");
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, Object> entry : bean.entrySet()) {
            if (!queryFieldLs.contains(entry.getKey()) && entry.getValue() != null) {
                sql.append(entry.getKey()).append(" = ? , ");
                values.add(entry.getValue());
            }
        }
        if (!values.isEmpty()) {
            sql = new StringBuilder(sql.substring(0, sql.length() - 2));
            if (!queryFieldLs.isEmpty()) {
                sql.append(" where ");
                for (String field : queryFieldLs) {
                    sql.append(field).append( " = ? and ");
                    values.add(bean.get(field));
                }
                sql = new StringBuilder(sql.substring(0, sql.length() - 5));
            }
            return update(sql.toString(), values.toArray());
        }
        return true;
    }


    private void initDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }
        if (pool.containsKey(key)) {
            dataSource = pool.get(key);
            return;
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
        config.setMaximumPoolSize(50);
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

    public Connection getConnection() {
        initDataSource();
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            pool.remove(key);
            e.printStackTrace();
            throw new RuntimeException("get jdbc connection fail");
        }
    }

    private void close(Connection con) {
        try {
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        DriverUtil driverUtil = DriverUtil.getMysqlInstance("192.168.4.12", 3306, "model", "root", "root@hiekn");
        System.out.println(driverUtil.count("select count(1) from t_user"));
    }
}