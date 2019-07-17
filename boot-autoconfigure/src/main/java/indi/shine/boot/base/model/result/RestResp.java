package indi.shine.boot.base.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class RestResp<T> {

    @JsonProperty("ErrorCode")
    private Integer code = 0;
    @JsonProperty("ErrorInfo")
    private String msg = "";
    @JsonProperty("ActionStatus")
    private String status = "OK";
    @JsonProperty("data")
    private T data;

    public RestResp() {
    }

    public RestResp(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        this.status = "FAIL";
    }

    public RestResp(T data) {
        this.data = data;
    }

    @JsonIgnore
    public Integer getCode() {
        return this.code;
    }

    @JsonIgnore
    public String getMsg() {
        return this.msg;
    }
}