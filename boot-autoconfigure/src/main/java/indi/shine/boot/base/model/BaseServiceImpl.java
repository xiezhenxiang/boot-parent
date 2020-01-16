package indi.shine.boot.base.model;

import indi.shine.boot.base.model.api.resp.ReturnPage;
import indi.shine.boot.base.model.search.QueryCondition;
import javax.annotation.Resource;
import java.util.List;

public class BaseServiceImpl<T, PK> implements BaseService<T, PK> {

    @Resource
    private BaseMapper<T, PK> baseMapper;

    @Override
    public ReturnPage<T> list(List<QueryCondition> qcList, List<QueryCondition> qoList, PageModel page) {
        List<T> ls = baseMapper.list(qcList, qoList, page.getPageNo(), page.getPageSize());
        Integer count = baseMapper.countList(qcList);
        return ReturnPage.of(ls, count);
    }

    @Override
    public T save(T pojo) {
        baseMapper.insert(pojo);
        return pojo;
    }

    @Override
    public T saveSelective(T pojo) {
        baseMapper.insertSelective(pojo);
        return pojo;
    }

    @Override
    public void deleteByPrimaryKey(PK id) {
        this.baseMapper.deleteByPrimaryKey(id);
    }

    @Override
    public T getByPrimaryKey(PK id) {
        return this.baseMapper.selectByPrimaryKey(id);
    }

    @Override
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