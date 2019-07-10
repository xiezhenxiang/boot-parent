package indi.shine.boot.base.exception.handler;


import indi.shine.boot.base.model.ValidationErrorBean;
import indi.shine.boot.base.model.result.RestResp;
import indi.shine.boot.base.util.ErrorMsgUtil;
import com.google.common.collect.Lists;
import org.glassfish.jersey.server.validation.ValidationError;
import org.glassfish.jersey.server.validation.internal.ValidationHelper;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Iterator;
import java.util.List;

/**
 * @author xiezhenxiang 2019/6/13
 **/
public final class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    @Context
    private Configuration config;

    public ValidationExceptionMapper() {
    }

    @Override
    public Response toResponse(ValidationException exception) {
        RestResp<List<ValidationErrorBean>> rs = new RestResp<>();
        rs.setCode(30001);
        rs.setMsg(ErrorMsgUtil.getErrMsg(30001));

        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException)exception;
            ResponseBuilder response = Response.status(ValidationHelper.getResponseStatus(cve));
            Object property = this.config.getProperty("jersey.config.beanValidation.enableOutputValidationErrorEntity.server");
            if (property != null && Boolean.valueOf(property.toString())) {
                response.type(MediaType.APPLICATION_JSON_TYPE);
                List<ValidationError> errors = ValidationHelper.constraintViolationToValidationErrors(cve);
                List<ValidationErrorBean> list = Lists.newArrayList();
                Iterator var8 = errors.iterator();

                while (var8.hasNext()) {
                    ValidationError error = (ValidationError)var8.next();
                    ValidationErrorBean validationErrorBean = new ValidationErrorBean();
                    validationErrorBean.setMessage(error.getMessage());
                    validationErrorBean.setInvalidValue(error.getInvalidValue());
                    validationErrorBean.setPath(error.getPath());
                    list.add(validationErrorBean);
                }

                rs.setData(list);
            }
        }

        return Response.ok(rs).build();
    }
}
