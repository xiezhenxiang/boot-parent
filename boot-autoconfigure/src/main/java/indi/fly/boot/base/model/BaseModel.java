package indi.fly.boot.base.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

public class BaseModel {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer pageNo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer pageSize;
    private transient Date createTime;
    private transient Date updateTime;

    public BaseModel() {
    }

    public Integer getPageNo() {
        return this.pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}