package indi.shine.boot.base.exception.handler;

import indi.shine.boot.base.model.api.resp.ReturnT;
import indi.shine.boot.base.util.CodeMsgUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

public final class WebApplicationExceptionHandler implements ExceptionMapper<WebApplicationException> {
    private static final Log logger = LogFactory.getLog(WebApplicationExceptionHandler.class);

    public WebApplicationExceptionHandler() {
    }

    @Override
    public Response toResponse(WebApplicationException exception) {
        int code = 80004;
        String errMsg = CodeMsgUtil.getMsg(code);
        Status statusCode = Status.OK;
        if (exception instanceof NotFoundException) {
            statusCode = Status.NOT_FOUND;
            code = 80001;
        } else if (exception instanceof NotAllowedException) {
            statusCode = Status.METHOD_NOT_ALLOWED;
            code = 80002;
        } else if (exception instanceof NotAcceptableException) {
            statusCode = Status.NOT_ACCEPTABLE;
            code = 80003;
        } else if (exception instanceof InternalServerErrorException) {
            statusCode = Status.INTERNAL_SERVER_ERROR;
        }

        logger.error(code, exception);
        return Response.ok(ReturnT.fail(code, errMsg)).status(statusCode).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
