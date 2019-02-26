package indi.fly.boot.base.exception;


public class ErrorCodes implements ExceptionKeys {

    private ErrorCodes() {
    }

    public static int ID_NOTEXIST_ERROR = 56014;
    public static int NAME_EXIST_ERROR = 56015;
    public static int PARENTS_CHILD_SAME_ERROR = 56016;
    public static int APK_ERROR = 56017;
    public static int FILE_MISS_ERROR = 56018;
    public static int FILE_SIZE_ERROR = 56019;
    public static int FILE_TYPE_ERROR = 56021;
    public static int FILE_IS_NULL = 56028;
    public static int FILE_TITLE_ERROR = 56020;
    public static int VERSION_CREATE_ERROR = 56022;
    public static int VERSION_DELETE_ERROR = 56023;
    public static int VERSION_CHANGE_ERROR = 56024;
    public static int ID_PAR_MISS = 56025;
    public static int ID_PAR_ONLEONE = 56026;
    public static int ID_PAR_SELF = 56027;
    public static int ID_PAR_MORE = 56029;
    public static int PARAM_TYPE_ERROR = 56030;
    public static int MONGO_ADD_ERROR = 56031;
    public static int ENTITY_NONE_EXIST = 56032;
    public static int FIELD_TYPE_ERROR =  56033;
    public static int CONCEPT_NOT_EXIST_ENTITY =  56034;
    public static int PAR_SELF_ERR =  56035;
    public static int PARAM_CONVERT_ERROR =  56036;
    public static int PRIVATE_ATTRNAME_NULL =  56037;
    public static int XSS_NULL =  56038;
    public static int ATTR_NAME_ERROR =  56039;
    public static int NAME_NULL_ERROR =  56040;
    public static int DOMAIN_FIELD_NULL_ERROR = 56041;
    public static int CLASS_ID_NOT_EXIST = 56042;
    public static int DATA_FORMAT_ERROR = 56043;
    public static int MERGE_SCORE_NOT_RIGHT = 56044;
    public static int ENTITY_NONE_IN_CONCEPT = 56045;
}
