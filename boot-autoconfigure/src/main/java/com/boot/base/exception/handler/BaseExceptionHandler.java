package com.boot.base.exception.handler;

import com.boot.base.exception.BaseException;
import com.boot.base.jersey.JerseySwaggerProperties;
import com.boot.base.model.result.RestResp;
import com.boot.base.util.BeanUtils;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;
import java.util.stream.Collectors;

public final class BaseExceptionHandler implements ExceptionMapper<BaseException> {
    private static final Log logger = LogFactory.getLog(BaseExceptionHandler.class);
    private String basePackage = ((JerseySwaggerProperties)BeanUtils.getBean(JerseySwaggerProperties.class)).getBasePackage();

    public BaseExceptionHandler() {
    }

    public Response toResponse(BaseException exception) {
        Integer code = exception.getCode();
        String errMsg = exception.getMsg();
        exception.setStackTrace((StackTraceElement[])((List)Lists.newArrayList(exception.getStackTrace()).stream().filter((s) -> {
            return s.getClassName().contains(this.basePackage);
        }).collect(Collectors.toList())).toArray(new StackTraceElement[0]));
        logger.error(code, exception);
        return Response.ok(new RestResp(code, errMsg)).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}