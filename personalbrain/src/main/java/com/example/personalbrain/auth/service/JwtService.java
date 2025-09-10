package com.example.personalbrain.auth.service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.personalbrain.user.model.User;
import com.example.personalbrain.user.repository.UserRepository;
import com.example.personalbrain.user.service.CustomUserDetailsService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;


@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expirationMs}")
    private long jwtExpirationMs;
    @Value("${app.jwt.refreshExpirationMs:1209600000}") // 14 days default
    private long refreshExpirationMs;
        // Allow small clock skew when validating (e.g., container clocks)
    private static final long ALLOWED_SKEW_SECONDS = 30L;
    private static final String AUTHORITIES_CLAIM = "roles";
    private final String issuer = "personalBrain.ai";
    private CustomUserDetailsService userDetailsService;
    public record JwtDebug(String issuer, String subject, Date issuedAt, Date expiration) {}

    private final UserRepository userRepository;
        public JwtService(CustomUserDetailsService userDetailsService, UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }
    public JwtDebug debugParse(String jwt) {
    var jws = Jwts.parser()
            .clockSkewSeconds(60)       // no requireIssuer here; just to read
            .verifyWith(accessKey())
            .build()
            .parseSignedClaims(jwt);
    var c = jws.getPayload();
    return new JwtDebug(c.getIssuer(), c.getSubject(), c.getIssuedAt(), c.getExpiration());
}
    public String generateToken(User user) {
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(user.getEmail())
                .claim("name", user.getFullName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(accessKey(), Jwts.SIG.HS256).compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email != null && email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    public Claims getClaims(String token) {
        return io.jsonwebtoken.Jwts.parser()
        .requireIssuer(issuer)     
        .clockSkewSeconds(30)                  
        .verifyWith(accessKey())                
        .build()
        .parseSignedClaims(token)              
        .getPayload();
    }


    private Key getSignKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    public String generateRefreshToken(UserDetails user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(exp)
                .signWith(refreshKey(), Jwts.SIG.HS256)
                .compact();
    }
    public boolean validateAccess(String jwt) {
        try {
            parseAccess(jwt);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public UserDetails loadUserFromAccess(String jwt) {
        Jws<Claims> jws = parseAccess(jwt);
        String username = jws.getPayload().getSubject();

        return userDetailsService.loadUserByUsername(username);
    }
    
    public User loadDomainUserFromAccess(String jwt) {
        String email = parseAccess(jwt).getPayload().getSubject();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
    public String extractSubject(String jwt) {
        return parseAccess(jwt).getPayload().getSubject();
    }


    private Jws<Claims> parseAccess(String jwt) {
        return Jwts.parser()
                .requireIssuer(issuer)
                .clockSkewSeconds(ALLOWED_SKEW_SECONDS)
                .verifyWith(accessKey())
                .build()
                .parseSignedClaims(jwt);
    }

    public boolean validateRefresh(String jwt) {
        try {
            parseRefresh(jwt);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractSubjectFromRefresh(String jwt) {
        return parseRefresh(jwt).getPayload().getSubject();
    }

    private Jws<Claims> parseRefresh(String jwt) {
        return Jwts.parser()
                .requireIssuer(issuer)
                .clockSkewSeconds(ALLOWED_SKEW_SECONDS)
                .verifyWith(refreshKey())
                .build()
                .parseSignedClaims(jwt);
    }

    private SecretKey accessKey() {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret.trim()));
        } catch (IllegalArgumentException badBase64) {
            return Keys.hmacShaKeyFor(jwtSecret.trim().getBytes(StandardCharsets.UTF_8));
        }
    }

    private SecretKey refreshKey() {
        return accessKey();
    }

}