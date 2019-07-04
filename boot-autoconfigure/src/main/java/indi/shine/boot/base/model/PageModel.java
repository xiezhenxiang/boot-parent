package indi.shine.boot.base.model;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

@Data
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
}
