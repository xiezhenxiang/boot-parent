package indi.shine.boot.base.model.api.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import indi.shine.boot.base.model.constant.BaseErrorCode;
import indi.shine.boot.base.model.constant.ErrorCodeMsg;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * api return model
 * @author xiezhenxiang 2020/1/7
 **/
@Data
@ApiModel
@JsonInclude(Include.NON_NULL)
public class ReturnT<T> implements Serializable  {

    private static final long serialVersionUID = 112L;
    private static final int SUCCESS_CODE = 200;
    private static final String SUCCESS_MSG = "ok";

    private int code;
    private String msg;
    private T content;

    private ReturnT(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private ReturnT(T content) {
        this.code = SUCCESS_CODE;
        this.msg = SUCCESS_MSG;
        this.content = content;
    }

    public static <T> ReturnT<T> success() {
        return new ReturnT<>(SUCCESS_CODE, SUCCESS_MSG);
    }

    public static <T> ReturnT<T> success(T content) {
        return new ReturnT<>(content);
    }

    public static <T> ReturnT<T> fail() {
        return fail(BaseErrorCode.UNKNOWN_ERROR);
    }

    public static <T> ReturnT<T> fail(int code, String msg) {
        return new ReturnT<>(code, msg);
    }

    public static <T> ReturnT<T> fail(int code) {
        return new ReturnT<>(code, ErrorCodeMsg.of(code));
    }
}