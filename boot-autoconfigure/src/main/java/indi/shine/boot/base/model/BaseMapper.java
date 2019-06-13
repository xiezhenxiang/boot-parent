package indi.shine.boot.base.model;

import indi.shine.boot.base.model.search.QueryCondition;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseMapper<T, PK> {
    /**
     * 插入
     * @author xiezhenxiang 2019/6/13
     * @param var1 插入数据
     * @return 影响行数
     **/
    int insert(T var1);

    /**
     * 插入，判断空值
     * @param var1 插入数据
     * @return 影响行数
     */
    int insertSelective(T var1);

    /**
     * 根据ID删除
     * @author xiezhenxiang 2019/6/13
     * @param var1 ID
     * @return 影响行数
     **/
    int deleteByPrimaryKey(PK var1);

    /**
     * 根据ID查找
     * @author xiezhenxiang 2019/6/13
     * @param var1 ID
     * @return 查找结果
     **/
    T selectByPrimaryKey(PK var1);

    /**
     * 根据主键更新，判断空值
     * @author xiezhenxiang 2019/6/13
     * @param var1 更新数据
     * @return 影响行数
     **/
    int updateByPrimaryKeySelective(T var1);

    /**
     * 分页查找
     * @author xiezhenxiang 2019/6/13
     * @param var1 查找条件
     * @return 返回结果
     **/
    List<T> pageSelect(T var1);

    /**
     * 分页查找结果总条数
     * @author xiezhenxiang 2019/6/13
     * @param var1 查找条件
     * @return 总数量
     **/
    int pageCount(T var1);

    /**
     * 根据条件查找
     * @author xiezhenxiang 2019/6/13
     * @param var1 查找条件
     * @return 查找结果
     **/
    List<T> selectByCondition(T var1);
    /**
     * 根据条件查找结果总数
     * @author xiezhenxiang 2019/6/13
     * @param qcList 查找条件
     * @return 总数量
     **/
    Integer countList(@Param("qcList") List<QueryCondition> qcList);

    /**
     * 根据条件查找
     * @author xiezhenxiang 2019/6/13
     * @param qcList 查找条件
     * @param qoList 排序条件
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 查找结果
     **/
    List<T> list(@Param("qcList") List<QueryCondition> qcList, @Param("qoList") List<QueryCondition> qoList, @Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize);


}