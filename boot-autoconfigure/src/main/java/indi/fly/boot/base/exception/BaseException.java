package indi.fly.boot.base.exception;

import indi.fly.boot.base.util.ErrorMsgUtil;

/**
 * 异常基类，各个模块的运行期异常均继承与该类 
 */
public class BaseException extends RuntimeException {
    private Integer code;
    private String msg;

    protected BaseException(Integer code) {
        this(code, (String)null);
    }

    protected BaseException(Integer code, String msg) {
        super(msg == null ? ErrorMsgUtil.getErrMsg(code) : ErrorMsgUtil.getErrMsg(code) + msg);
        this.code = code;
        this.msg = msg == null ? ErrorMsgUtil.getErrMsg(code) : ErrorMsgUtil.getErrMsg(code) + msg;
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
