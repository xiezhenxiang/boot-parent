package indi.shine.boot.base.model.constant;

/**
 * base error code
 * @author xiezhenxiang 2020/1/8
 **/
public interface BaseErrorCode {

    int JSON_PARSE_ERROR = 1000;
    int DATA_EXISTS = 1001;

    int REQUEST_BAD = 4400;
    int REQUEST_DENY = 4403;
    int REQUEST_NOT_FIND = 4404;
    int REQUEST_REPEAT = 4405;
}
