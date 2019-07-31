package indi.shine.boot.base.exception;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * @author xiezhenxiang 2019/7/31
 */
public class Assert {

    private static final Integer ERROR_CODE = 500;

    public static void isTrue(boolean expression, String message) {

        if (!expression) {
            throw ServiceException.newInstance(ERROR_CODE, message);
        }
    }

    public static void isTrue(boolean expression) {

        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    public static void notNull(Object object, String message) {

        if (object == null) {
            throw ServiceException.newInstance(ERROR_CODE, message);
        }
    }

    public static void notNull(Object object) {

        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    public static void notEmpty(Collection<?> collection, String message) {

        if (CollectionUtils.isEmpty(collection)) {
            throw ServiceException.newInstance(ERROR_CODE, message);
        }
    }

    public static void notEmpty(Collection<?> collection) {

        notEmpty(collection, "[Assertion failed] - this collection must not be empty: it must contain at least 1 element");
    }

    private static void instanceCheckFailed(Class<?> type, Object obj, String msg) {

        String className = obj != null ? obj.getClass().getName() : "null";
        String result = "";
        boolean defaultMessage = true;
        if (StringUtils.hasLength(msg)) {
            if (endsWithSeparator(msg)) {
                result = msg + " ";
            } else {
                result = messageWithTypeName(msg, className);
                defaultMessage = false;
            }
        }

        if (defaultMessage) {
            result = result + "Object of class [" + className + "] must be an instance of " + type;
        }

        throw ServiceException.newInstance(ERROR_CODE, result);
    }

    public static void isInstanceOf(Class<?> type, Object obj, String message) {

        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            instanceCheckFailed(type, obj, message);
        }
    }

    public static void isInstanceOf(Class<?> type, Object obj) {

        isInstanceOf(type, obj, "");
    }

    private static boolean endsWithSeparator(String msg) {

        return msg.endsWith(":") || msg.endsWith(";") || msg.endsWith(",") || msg.endsWith(".");
    }

    private static String messageWithTypeName(String msg, Object typeName) {

        return msg + (msg.endsWith(" ") ? "" : ": ") + typeName;
    }
}
