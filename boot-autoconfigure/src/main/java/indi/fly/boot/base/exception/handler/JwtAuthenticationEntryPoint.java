package indi.fly.boot.base.exception.handler;

/*
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.fly.boot.base.exception.InvalidAuthenticationTokenException;
import RestResp;
import RestResp.ActionStatusMethod;
import ErrorMsgUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private ObjectMapper mapper;

    public JwtAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        RestResp restResp = new RestResp();
        restResp.setActionStatus(ActionStatusMethod.FAIL.toString());
        HttpStatus status;
        if (authException instanceof InvalidAuthenticationTokenException) {
            status = HttpStatus.UNAUTHORIZED;
            restResp.setErrorCode(Integer.valueOf(30016));
            restResp.setErrorInfo(ErrorMsgUtil.getErrMsg(Integer.valueOf(30016)));
        } else {
            status = HttpStatus.FORBIDDEN;
            restResp.setErrorCode(Integer.valueOf(30012));
            restResp.setErrorInfo(ErrorMsgUtil.getErrMsg(Integer.valueOf(30012)));
        }

        response.setStatus(status.value());
        response.setContentType("application/json");
        this.mapper.writeValue(response.getWriter(), restResp);
    }
}

*/
