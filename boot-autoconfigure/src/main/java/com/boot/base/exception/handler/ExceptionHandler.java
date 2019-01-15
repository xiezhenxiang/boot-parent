package com.boot.base.exception.handler;

import com.boot.base.model.result.RestResp;
import com.boot.base.util.ErrorMsgUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public final class ExceptionHandler implements ExceptionMapper<Exception> {
    private static final Log logger = LogFactory.getLog(ExceptionHandler.class);

    public ExceptionHandler() {
    }

    public Response toResponse(Exception exception) {
        Integer code = 90000;
        String errMsg = ErrorMsgUtil.getErrMsg(code);
        logger.error(code, exception);
        return Response.ok(new RestResp(code, errMsg)).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
