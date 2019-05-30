package indi.shine.boot.base.model;

import indi.shine.boot.base.model.result.RestData;
import indi.shine.boot.base.model.search.QueryCondition;

import java.util.List;

public interface BaseService<T, PK> {
    T save(T var1);

    T saveSelective(T var1);

    void deleteByPrimaryKey(PK var1);

    T getByPrimaryKey(PK var1);

    void updateByPrimaryKeySelective(T var1);

    RestData<T> list(List<QueryCondition> qcList, List<QueryCondition> qoList, PageModel page);
}