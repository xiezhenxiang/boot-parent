package indi.shine.boot.base.model;

import indi.shine.boot.base.model.result.RestData;
import indi.shine.boot.base.model.search.QueryCondition;

import java.util.List;

public interface BaseService<T, PK> {
    /**
     * 保存
     * @author xiezhenxiang 2019/6/13
     * @param var1 插入数据
     * @return 保存的数据
     **/
    T save(T var1);

    /**
     * 保存, 判断空值
     * @author xiezhenxiang 2019/6/13
     * @param var1 插入数据
     * @return 保存的数据
     **/
    T saveSelective(T var1);

    /**
     * 根据主键ID删除
     * @author xiezhenxiang 2019/6/13
     * @param var1 主键ID
     **/
    void deleteByPrimaryKey(PK var1);

    /**
     * 根据ID查找
     * @author xiezhenxiang 2019/6/13
     * @param var1 ID
     **/
    T getByPrimaryKey(PK var1);

    /**
     * 根据ID更新，判断空值
     * @author xiezhenxiang 2019/6/13
     * @param var1 更新数据
     **/
    void updateByPrimaryKeySelective(T var1);

    /**
     * 条件查找
     * @author xiezhenxiang 2019/6/13
     * @param qcList 查找条件
     * @param qoList 排序条件
     * @param page 分页信息
     * @return 返回查找结果
     **/
    RestData<T> list(List<QueryCondition> qcList, List<QueryCondition> qoList, PageModel page);
}