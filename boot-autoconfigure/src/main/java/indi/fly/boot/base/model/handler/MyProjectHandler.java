package indi.fly.boot.base.model.handler;
import indi.fly.boot.base.util.JsonUtils;
import com.google.gson.reflect.TypeToken;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MyProjectHandler extends BaseTypeHandler<List<String>> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<String> strings, JdbcType jdbcType) throws SQLException {
        String param = JsonUtils.toJson(strings);
        preparedStatement.setString(i, param);
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        String str = resultSet.getString(columnName);
        List<String> inputBean = JsonUtils.fromJson(str, new TypeToken<List<String>>(){}.getType());
        return inputBean;
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String str = resultSet.getString(i);
        List<String> inputBean = JsonUtils.fromJson(str, new TypeToken<List<String>>(){}.getType());
        return inputBean;
    }

    @Override
    public List<String> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String str = callableStatement.getString(i);
        List<String> inputBean = JsonUtils.fromJson(str, new TypeToken<List<String>>(){}.getType());
        return inputBean;
    }
}
