package indi.shine.boot.base.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class RestResp<T> {

    @JsonProperty("status")
    private String status = "OK";
    @JsonProperty("errorCode")
    private Integer errorCode = 0;
    @JsonProperty("errorInfo")
    private String errorInfo = "";

    private T data;

    public RestResp(Integer code, String msg) {
        this.errorCode = code;
        this.errorInfo = msg;
        this.status = "FAIL";
    }

    public RestResp(){}

    public RestResp(T data) {
        this.data = data;
    }

    @JsonIgnore
    public Integer getErrorCode() {
        return this.errorCode;
    }

    @JsonIgnore
    public String getErrorInfo() {
        return this.errorInfo;
    }
}