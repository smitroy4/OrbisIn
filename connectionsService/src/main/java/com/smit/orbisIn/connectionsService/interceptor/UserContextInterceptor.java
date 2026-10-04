package com.smit.orbisIn.connectionsService.interceptor;

import com.smit.orbisIn.connectionsService.context.ContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that extracts the userId from the incoming request header and stores it in {@link ContextHolder}.
 */
@Component
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader(USER_ID_HEADER);
        if (header != null) {
            try {
                Long userId = Long.valueOf(header);
                ContextHolder.setUserId(userId);
            } catch (NumberFormatException e) {
                log.warn("Invalid userId header value: {}", header);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ContextHolder.clear();
    }
}
