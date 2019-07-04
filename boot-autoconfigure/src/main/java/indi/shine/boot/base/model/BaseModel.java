package indi.shine.boot.base.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
public class BaseModel {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer pageNo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer pageSize;
    private transient Date createTime;
    private transient Date updateTime;
}