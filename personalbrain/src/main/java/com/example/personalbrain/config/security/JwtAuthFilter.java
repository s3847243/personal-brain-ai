package com.example.personalbrain.config.security;


import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.personalbrain.auth.service.JwtService;
import com.example.personalbrain.user.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        // Run during async/ERROR dispatches as well
        return false;
    }
    private String readAccessCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if ("accessToken".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        // Optional: also run on ERROR dispatch to avoid denials there
        return false;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);
        System.out.println("token: "+token);
        if (token != null) {
            String email = jwtService.extractEmail(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

                        System.out.println("🔍 Filter triggered for: " + request.getRequestURI());

                        if (token != null) {
                            System.out.println("✅ JWT found, extracted email: " + email);
                        }
            }
        }


        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // 1. First try Authorization header
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            System.out.println("🔐 Token from Authorization header");
            return header.substring(7);
        }
        System.out.println("requesttt"+request);
        // 2. Then try HttpOnly Cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                                    System.out.println("🍪 Token from Cookie");

                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
