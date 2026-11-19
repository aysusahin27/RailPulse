package com.basespringboot.security;

import com.basespringboot.entity.UserEntity;
import com.basespringboot.exception.ForbiddenException;
import com.basespringboot.exception.UnauthorizedException;
import com.basespringboot.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireAuth authAnnotation = handlerMethod.getMethodAnnotation(RequireAuth.class);
        if (authAnnotation == null) {
            authAnnotation = handlerMethod.getBeanType().getAnnotation(RequireAuth.class);
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        }

        if (token != null && jwtService.validateToken(token)) {
            String username = jwtService.extractUsername(token);
            UserEntity user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                UserContext.setCurrentUser(user);
                request.setAttribute("currentUser", user);
            }
        }

        if (authAnnotation != null) {
            UserEntity user = UserContext.getCurrentUser();
            if (user == null) {
                throw new UnauthorizedException("Authentication required: missing or invalid Bearer token");
            }

            String requiredRole = authAnnotation.role();
            if (!requiredRole.isBlank() && !requiredRole.equalsIgnoreCase(user.getRole())) {
                throw new ForbiddenException("Access denied: required role " + requiredRole);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
