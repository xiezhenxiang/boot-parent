package com.boot.base.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResp<T> {

    @JsonProperty("ActionStatus")
    private String ActionStatus;
    @JsonProperty("ErrorCode")
    private Integer ErrorCode;
    @JsonProperty("ErrorInfo")
    private String ErrorInfo;
    private T data;

    public RestResp() {
        this.ActionStatus = ActionStatusMethod.OK.toString();
        this.ErrorCode = Integer.valueOf(0);
        this.ErrorInfo = "";
    }

    public RestResp(Integer code, String msg) {
        this.ActionStatus = ActionStatusMethod.OK.toString();
        this.ErrorCode = Integer.valueOf(0);
        this.ErrorInfo = "";
        this.ActionStatus = ActionStatusMethod.FAIL.toString();
        this.ErrorCode = code;
        this.ErrorInfo = msg;
    }

    public RestResp(T data) {
        this.ActionStatus = ActionStatusMethod.OK.toString();
        this.ErrorCode = Integer.valueOf(0);
        this.ErrorInfo = "";
        this.data = data;
    }

    @JsonIgnore
    public String getActionStatus() {
        return this.ActionStatus;
    }

    public void setActionStatus(String actionStatus) {
        this.ActionStatus = actionStatus;
    }

    @JsonIgnore
    public Integer getErrorCode() {
        return this.ErrorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.ErrorCode = errorCode;
    }

    @JsonIgnore
    public String getErrorInfo() {
        return this.ErrorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.ErrorInfo = errorInfo;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static enum ActionStatusMethod {
        OK("OK"),
        FAIL("FAIL");

        private final String name;

        private ActionStatusMethod(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}