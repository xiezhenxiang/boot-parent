package indi.shine.boot.base.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class RestResp<T> {

    @JsonProperty("status")
    private String status;
    @JsonProperty("code")
    private Integer errorCode;
    @JsonProperty("msg")
    private String errorInfo;
    private T data;

    public static final String SUCCESS_STATUS = "ok";
    public static final String ERROR_STATUS = "fail";

    public RestResp() {
        this.status = SUCCESS_STATUS;
        this.errorCode = 200;
        this.errorInfo = "";
    }

    public RestResp(Integer code, String msg) {
        this.status = SUCCESS_STATUS;
        this.errorCode = 200;
        this.errorInfo = "";
        this.status = ERROR_STATUS;
        this.errorCode = code;
        this.errorInfo = msg;
    }

    public RestResp(T data) {
        this.status = SUCCESS_STATUS;
        this.errorCode = 200;
        this.errorInfo = "";
        this.data = data;
    }

    @JsonIgnore
    public String getStatus() {
        return this.status;
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