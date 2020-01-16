package indi.shine.boot.base.exception;

import indi.shine.boot.base.util.CodeMsgUtil;

/**
 * @author xiezhenxiang 2019/6/13
 **/
public class BaseException extends RuntimeException {
    private Integer code;
    private String msg;

    protected BaseException(Integer code) {
        this(code, null);
    }

    protected BaseException(Integer code, String msg) {
        super(msg == null ? CodeMsgUtil.getMsg(code) :  msg);
        this.code = code;
        this.msg = msg == null ? CodeMsgUtil.getMsg(code) :  msg;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
