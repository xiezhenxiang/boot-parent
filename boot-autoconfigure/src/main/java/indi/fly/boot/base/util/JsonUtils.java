package indi.fly.boot.base.util;

import indi.fly.boot.base.exception.JsonException;
import com.google.gson.Gson;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;

@Configuration
public class JsonUtils {

	private static Gson gson = new Gson();

	public JsonUtils() {
	}

	public static <T> T fromJson(String json, Class<T> cls) {
		try {
			return gson.fromJson(json, cls);
		} catch (Exception var3) {
			throw JsonException.newInstance();
		}
	}

	public static <T> T fromJson(String json, Type typeOfT) {
		try {
			return gson.fromJson(json, typeOfT);
		} catch (Exception var3) {
			throw JsonException.newInstance();
		}
	}

	public static String toJson(Object obj) {
		try {
			return gson.toJson(obj);
		} catch (Exception var2) {
			throw JsonException.newInstance();
		}
	}

}