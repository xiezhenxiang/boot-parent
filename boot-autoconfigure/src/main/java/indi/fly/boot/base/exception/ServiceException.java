package indi.fly.boot.base.exception;


public class ServiceException extends BaseException {
	private ServiceException(Integer code) {
		super(code);
	}

	public static ServiceException newInstance() {
		return newInstance(90000);
	}

	public static ServiceException newInstance(Integer code) {
		return new ServiceException(code);
	}
}
