package com.example.zorvyn.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> globalBuckets = new ConcurrentHashMap<>();

    private Bucket createLoginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build();
    }

    private Bucket createGlobalBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getServletPath();

        if (path.contains("/auth/login")) {
            String ip = request.getRemoteAddr();
            Bucket bucket = loginBuckets.computeIfAbsent(ip, k -> createLoginBucket());
            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit exceeded for login from IP: {}", ip);
                sendTooManyRequests(response);
                return false;
            }
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !auth.getPrincipal().equals("anonymousUser")) {
            String email = auth.getName();
            Bucket bucket = globalBuckets.computeIfAbsent(email, k -> createGlobalBucket());
            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit exceeded for user: {}", email);
                sendTooManyRequests(response);
                return false;
            }
        }

        return true;
    }

    private void sendTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.setHeader("Retry-After", "60");
        response.getWriter().write(
                "{\"success\":false,\"message\":\"Too many requests - please slow down\",\"status\":429}");
    }
}
