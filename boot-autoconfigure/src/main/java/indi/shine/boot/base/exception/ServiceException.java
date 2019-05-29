package indi.shine.boot.base.exception;


public class ServiceException extends BaseException {
	private ServiceException(Integer code) {
		super(code);
	}

	private ServiceException(Integer code, String msg) {
		super(code, msg);
	}

	public static ServiceException newInstance() {
		return newInstance(90000);
	}

	public static ServiceException newInstance(Integer code) {
		return new ServiceException(code);
	}

	public static ServiceException newInstance(Integer code, String msg) {
		return new ServiceException(code, msg);
	}
}
