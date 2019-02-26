package indi.fly.boot.base.filter;

import indi.fly.boot.base.exception.ServiceException;
import indi.fly.boot.base.jwt.JwtToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationTokenFilter(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        System.out.println("ttt" + header);
        if (header != null) {
            try {
                UsernamePasswordAuthenticationToken authentication = this.getAuthentication(new JwtToken(request, response));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AuthenticationException var6) {
                SecurityContextHolder.clearContext();
                this.authenticationEntryPoint.commence(request, response, var6);
                return;
            }

            chain.doFilter(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(JwtToken jwtToken) {
        String token = jwtToken.getToken();
        if (token != null) {
            try {
                String user = jwtToken.getUserIdAsString();
                if (user != null) {
                    return new UsernamePasswordAuthenticationToken(user, (Object)null, new ArrayList());
                }
            } catch (Exception var4) {
                throw ServiceException.newInstance();
            }
        }

        return null;
    }
}