package indi.shine.boot.base.model;

import io.swagger.annotations.ApiParam;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.util.Objects;

public class PageModel extends BaseModel {

    @ApiParam("当前页，默认1")
    @DefaultValue("1")
    @Min(1L)
    @QueryParam("pageNo")
    private Integer pageNo;
    @ApiParam("每页数，默认10")
    @DefaultValue("10")
    @Max(50L)
    @QueryParam("pageSize")
    private Integer pageSize;

    public PageModel() {
    }

    public PageModel(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public Integer getPageNo() {
        return Objects.isNull(this.pageNo) ? this.pageNo : (this.pageNo.intValue() - 1) * this.pageSize.intValue();
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
}
