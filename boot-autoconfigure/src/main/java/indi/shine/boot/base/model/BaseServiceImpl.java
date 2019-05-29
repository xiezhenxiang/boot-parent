package indi.shine.boot.base.model;

import indi.shine.boot.base.model.result.RestData;
import indi.shine.boot.base.model.search.QueryCondition;

import javax.annotation.Resource;
import java.util.List;

public class BaseServiceImpl<T, PK> implements BaseService<T, PK> {

    @Resource
    private BaseMapper<T, PK> baseMapper;



    public BaseServiceImpl() {
    }

    @Override
    public RestData<T> list(List<QueryCondition> qcList, List<QueryCondition> qoList, PageModel page) {

        List<T> ls = baseMapper.list(qcList, qoList, page.getPageNo(), page.getPageSize());
        Integer count = baseMapper.countList(qcList);

        return new RestData<T>(ls, count);
    }

    public T save(T pojo) {
        baseMapper.insert(pojo);
        return pojo;
    }

    public T saveSelective(T pojo) {
        baseMapper.insertSelective(pojo);
        return pojo;
    }

    public void deleteByPrimaryKey(PK id) {
        this.baseMapper.deleteByPrimaryKey(id);
    }

    public T getByPrimaryKey(PK id) {
        return this.baseMapper.selectByPrimaryKey(id);
    }

    public void updateByPrimaryKeySelective(T pojo) {
        this.baseMapper.updateByPrimaryKeySelective(pojo);
    }


    public List<T> pageSelect(T pojo) {
        return baseMapper.pageSelect(pojo);
    }

    public int pageCount(T pojo) {
        return baseMapper.pageCount(pojo);
    }

    public List<T> selectByCondition(T pojo) {
        return baseMapper.selectByCondition(pojo);
    }
}