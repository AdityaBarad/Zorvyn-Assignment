package com.example.zorvyn.util;

import com.example.zorvyn.model.entity.User;
import com.example.zorvyn.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            return "anonymous";
        }
        return auth.getName();
    }

    public static Long getCurrentUserId(UserRepository userRepository) {
        String email = getCurrentUserEmail();
        if ("anonymous".equals(email)) {
            return null;
        }
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .map(User::getId)
                .orElse(null);
    }
}

