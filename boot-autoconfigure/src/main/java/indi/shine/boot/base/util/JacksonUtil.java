package indi.shine.boot.base.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jackson util
 * 1、obj need private and set/get；
 * 2、do not support inner class；
 * @author xuxueli 2015-9-25 18:02:56
 */
public class JacksonUtil {
	private static Logger logger = LoggerFactory.getLogger(JacksonUtil.class);

	private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	public static ObjectMapper getInstance() {
		return OBJECT_MAPPER;
	}
	static {
		OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * bean、array、List、Map --> json
	 * @param obj obj
	 * @return json string
	 */
	public static String writeValueAsString(Object obj) {
		try {
			return getInstance().writeValueAsString(obj);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * string --> bean、Map、List(array)
	 * @return obj
	 */
	public static <T> T readValue(String jsonStr, Class<T> clazz) {
		try {
			return getInstance().readValue(jsonStr, clazz);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * string --> List<Bean>...
	 * @param jsonStr
	 * @param parametrized
	 * @param parameterClasses
	 * @param <T>
	 * @return
	 */
	public static <T> T readValue(String jsonStr, Class<?> parametrized, Class<?>... parameterClasses) {
		try {
			JavaType javaType = getInstance().getTypeFactory().constructParametricType(parametrized, parameterClasses);
			return getInstance().readValue(jsonStr, javaType);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static <T> T readValueRefer(String jsonStr, TypeReference type) {
		try {
			return getInstance().readValue(jsonStr, new TypeReference<T>() { });
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static void main(String[] args) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("aaa", "111");
			map.put("bbb", 222);
			String json = writeValueAsString(map);
			System.out.println(json);
			System.out.println(readValue(json, Map.class));
			json = writeValueAsString(Lists.newArrayList(map));
			List<Map<String, Object>> ls = readValue(json, List.class, Map.class);
			System.out.println(writeValueAsString(ls));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}