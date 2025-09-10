package com.example.personalbrain.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.personalbrain.auth.dto.AuthResponse;
import com.example.personalbrain.auth.dto.LoginRequest;
import com.example.personalbrain.auth.dto.RegisterRequest;
import com.example.personalbrain.auth.model.RefreshToken;
import com.example.personalbrain.auth.service.AuthService;
import com.example.personalbrain.auth.service.JwtService;
import com.example.personalbrain.auth.service.RefreshTokenService;
import com.example.personalbrain.user.dto.UserDTO;
import com.example.personalbrain.user.model.User;
import com.example.personalbrain.user.model.UserPrincipal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }
   
    private static final boolean SAME_ORIGIN_DEV = true;

    private static final String SAMESITE = SAME_ORIGIN_DEV ? "Lax" : "None";
    private static final boolean SECURE = SAME_ORIGIN_DEV ? false : true;

    // Cookie names & paths
    private static final String ACCESS_COOKIE = "accessToken";
    private static final String REFRESH_COOKIE = "refreshToken";
    private static final String ACCESS_PATH = "/";           // send on all API routes
    private static final String REFRESH_PATH = "/api/auth";  // only needed for /api/auth/refresh

    // Lifetimes
    private static final Duration ACCESS_TTL = Duration.ofMinutes(15);
    private static final Duration REFRESH_TTL = Duration.ofDays(7);

    // ====== HELPERS ======
    private void setAccessCookie(HttpServletResponse res, String jwt) {
        ResponseCookie c = ResponseCookie.from(ACCESS_COOKIE, jwt)
            .httpOnly(true)
            .secure(SECURE)
            .sameSite(SAMESITE)
            .path(ACCESS_PATH)
            .maxAge(ACCESS_TTL)
            .build();
        res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
    }

  

    private void clearCookie(HttpServletResponse res, String name, String path) {
        ResponseCookie c = ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(SECURE)
            .sameSite(SAMESITE)
            .path(path)
            .maxAge(0)
            .build();
        res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
    }

    private String readCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        AuthResponse auth = authService.login(req); // must return access + refresh + user

        

        ResponseCookie refresh = ResponseCookie.from("refreshToken", auth.getRefreshToken())
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/api/auth")      // sent to /api/auth/refresh
            .maxAge(Duration.ofDays(7))
            .build();
        ResponseCookie access = ResponseCookie.from("accessToken", auth.getAccessToken())
            .httpOnly(true)
            .secure(false)          // same-origin dev (proxy). If you go cross-site+HTTPS: true + SameSite=None
            .sameSite("Lax")
            .path("/")              // MUST be "/"
            .maxAge(Duration.ofMinutes(15))
            .build();
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, access.toString())
            .header(HttpHeaders.SET_COOKIE, refresh.toString())
            .body(Map.of("user", auth.getUser()));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken,
                                          HttpServletResponse response) {
        System.out.println("refresh hit; refreshToken present? " + (refreshToken != null));

        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        // Your service verifies persistence + expiry
        RefreshToken tokenEntity = refreshTokenService.findByToken(refreshToken)
            .map(refreshTokenService::verifyExpiration)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        User user = tokenEntity.getUser();

       

        String newAccess = jwtService.generateToken(user);
        setAccessCookie(response, newAccess);

        // 204 is fine; frontend interceptor doesn’t need a body
        return ResponseEntity.noContent().build();
    }

    
    @GetMapping("/verify")
    public ResponseEntity<?> verify(HttpServletRequest request) {
        String access = readCookie(request, ACCESS_COOKIE);
        if (access == null) {
            System.out.println("[verify] no access cookie");
            return ResponseEntity.status(401).build();
    }

    try {
        var info = jwtService.debugParse(access);  // <— add method below
        System.out.println("[verify] jwt ok-ish: iss=" + info.issuer()
                + " sub=" + info.subject()
                + " exp=" + info.expiration());
    } catch (Exception e) {
        System.out.println("[verify] jwt parse FAILED: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
        return ResponseEntity.status(401).build();
    }

    if (!jwtService.validateAccess(access)) {
        System.out.println("[verify] validateAccess=false (issuer/exp/key likely mismatch)");
        return ResponseEntity.status(401).build();
    }

    try {
        var user = jwtService.loadDomainUserFromAccess(access);  // add in service, see below
        return ResponseEntity.ok(Map.of("user", UserDTO.from(user)));
    } catch (Exception e) {
        System.out.println("[verify] load user FAILED: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
        return ResponseEntity.status(401).build();
    }
}
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        clearCookie(response, ACCESS_COOKIE, ACCESS_PATH);
        clearCookie(response, REFRESH_COOKIE, REFRESH_PATH);
        return ResponseEntity.noContent().build();
    }

    // Handle auth service rejections as 401
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(401).body(Map.of("error", ex.getMessage()));
    }
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
}
