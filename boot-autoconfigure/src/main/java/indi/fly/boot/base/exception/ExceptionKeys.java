package indi.fly.boot.base.exception;

public interface ExceptionKeys {
    int PARAM_PARSE_ERROR = 30001;
    int JSON_PARSE_ERROR = 30002;
    int INIT_ERROR = 30003;
    int VERIFY_CODE_ERROR = 30004;
    int EXIST_ERROR = 30005;
    int NOT_FOUND_ERROR = 30006;
    int USER_PWD_ERROR = 30007;
    int PWD_ERROR = 30008;
    int GET_MOBILE_CODE_ERROR = 30009;
    int VERIFY_MOBILE_CODE_ERROR = 30010;
    int UPLOAD_ERROR = 30011;
    int PERMISSION_NOT_ENOUGH_ERROR = 30012;
    int ADD_ERROR = 30013;
    int DELETE_ERROR = 30014;
    int UPDATE_ERROR = 30015;
    int AUTHENTICATION_ERROR = 30016;
    int INVALID_CERTIFICATE_ERROR = 30017;

    int UN_LOGIN_ERROR = 50001;
    int ID_NOTEXIST_ERROR = 50002;
    int FILE_MISS_ERROR = 50003;
    int FILE_SIZE_ERROR = 50004;
    int FILE_TYPE_ERROR = 50005;
    int DATA_FORMAT_ERROR = 50006;

    int UNKNOWN_ERROR = 70001;

    int HTTP_ERROR = 80001;

    int SERVICE_ERROR = 90000;
    int THIRD_PARTY_ERROR = 90001;
    int REMOTE_SERVICE_ERROR = 90002;
    int REMOTE_DATA_PARSE_ERROR = 90003;
}