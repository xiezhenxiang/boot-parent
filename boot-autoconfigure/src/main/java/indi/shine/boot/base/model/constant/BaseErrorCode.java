package indi.shine.boot.base.model.constant;

/**
 * base error code
 * @author xiezhenxiang 2020/1/8
 **/
public interface BaseErrorCode {

    int JSON_PARSE_ERROR = 1000;

    int REQUEST_BAD = 4400;
    int REQUEST_DENY = 4403;
    int REQUEST_NOT_FIND = 4404;
    int REQUEST_REPEAT = 4405;

    int UNKNOWN_ERROR = 5000;
    int SOURCE_EXISTS = 5001;
    int PASSWORD_ERROR = 5002;
    int CHECK_CODE_ERROR = 5003;
}
