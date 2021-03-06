package indi.shine.boot.base.exception;

public class ThirdPartyException extends BaseException {

    private ThirdPartyException(Integer code) {
        super(code);
    }

    private ThirdPartyException(Integer code, String msg) {
        super(code, msg);
    }

    public static ThirdPartyException newInstance() {
        return newInstance(90002);
    }

    public static ThirdPartyException newInstance(Integer code) {
        return new ThirdPartyException(code);
    }

    public static ThirdPartyException newInstance(Integer code, String msg) {
        return new ThirdPartyException(code, msg);
    }
}
