package indi.shine.boot.base.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResp<T> {

    @JsonProperty("ActionStatus")
    private String actionStatus;
    @JsonProperty("ErrorCode")
    private Integer errorCode;
    @JsonProperty("ErrorInfo")
    private String errorInfo;
    private T data;

    public RestResp() {
        this.actionStatus = ActionStatusMethod.OK.toString();
        this.errorCode = 0;
        this.errorInfo = "";
    }

    public RestResp(Integer code, String msg) {
        this.actionStatus = ActionStatusMethod.OK.toString();
        this.errorCode = 0;
        this.errorInfo = "";
        this.actionStatus = ActionStatusMethod.FAIL.toString();
        this.errorCode = code;
        this.errorInfo = msg;
    }

    public RestResp(T data) {
        this.actionStatus = ActionStatusMethod.OK.toString();
        this.errorCode = 0;
        this.errorInfo = "";
        this.data = data;
    }

    @JsonIgnore
    public String getActionStatus() {
        return this.actionStatus;
    }

    public void setActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
    }

    @JsonIgnore
    public Integer getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    @JsonIgnore
    public String getErrorInfo() {
        return this.errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public enum ActionStatusMethod {
        // 成功
        OK("OK"),
        // 失败
        FAIL("FAIL");

        private final String name;

        ActionStatusMethod(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return this.name;
        }
    }
}