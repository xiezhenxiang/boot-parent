package indi.shine.boot.base.exception;


public class JsonException extends BaseException {

	private JsonException(Integer code) {
		super(code);
	}

	public static JsonException newInstance() {
		return newInstance(30002);
	}

	public static JsonException newInstance(Integer code) {
		return new JsonException(code);
	}
}
