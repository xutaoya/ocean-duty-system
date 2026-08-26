package com.oceanduty.interceptor;

import com.oceanduty.common.anno.RequireRole;
import com.oceanduty.common.constant.ResponseCodeConst;
import com.oceanduty.common.domain.RequestUser;
import com.oceanduty.common.exception.BusinessException;
import com.oceanduty.util.JwtUtil;
import com.oceanduty.util.RequestUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 登录与角色权限拦截器
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResponseCodeConst.NOT_LOGIN);
        }

        RequestUser requestUser;
        try {
            requestUser = jwtUtil.parseToken(token);
        } catch (Exception ex) {
            throw new BusinessException(ResponseCodeConst.NOT_LOGIN);
        }
        RequestUserContext.set(requestUser);
        checkRole(handlerMethod, requestUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        RequestUserContext.clear();
    }

    private void checkRole(HandlerMethod handlerMethod, RequestUser requestUser) {
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return;
        }
        boolean allowed = Arrays.stream(requireRole.value())
                .anyMatch(role -> role.equals(requestUser.getRole()));
        if (!allowed) {
            throw new BusinessException(ResponseCodeConst.NO_PERMISSION);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}
