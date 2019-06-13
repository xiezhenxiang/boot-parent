package indi.shine.boot.base.exception.handler;

import indi.shine.boot.base.model.result.RestResp;
import indi.shine.boot.base.util.ErrorMsgUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * @author xiezhenxiang 2019/6/13
 **/
public final class ExceptionHandler implements ExceptionMapper<Exception> {
    private static final Log logger = LogFactory.getLog(ExceptionHandler.class);

    public ExceptionHandler() {
    }

    @Override
    public Response toResponse(Exception exception) {
        Integer code = 90000;
        String errMsg = ErrorMsgUtil.getErrMsg(code);
        logger.error(code, exception);
        return Response.ok(new RestResp(code, errMsg)).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
