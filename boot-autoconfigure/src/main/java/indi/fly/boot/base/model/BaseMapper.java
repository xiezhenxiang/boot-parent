package indi.fly.boot.base.model;

import indi.fly.boot.base.model.search.QueryCondition;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseMapper<T, PK> {
    int insert(T var1);

    int insertSelective(T var1);

    int deleteByPrimaryKey(PK var1);

    T selectByPrimaryKey(PK var1);

    int updateByPrimaryKeySelective(T var1);

    List<T> pageSelect(T var1);

    int pageCount(T var1);

    List<T> selectByCondition(T var1);

    Integer countList(@Param("qcList") List<QueryCondition> qcList);

    List<T> list(@Param("qcList") List<QueryCondition> qcList, @Param("qoList") List<QueryCondition> qoList, @Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize);


}