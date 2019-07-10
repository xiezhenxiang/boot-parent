package indi.shine.boot.base.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class RestResp<T> {

    @JsonProperty("code")
    private Integer code = 200;
    @JsonProperty("msg")
    private String msg = "ok";
    private T data;

    public RestResp() {
    }

    public RestResp(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
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