package uz.consortgroup.userservice.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.security.ResetTokenStore;
import uz.consortgroup.userservice.service.impl.HasId;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {

    @Value("${security.token}")
    private String jetSecret;

    @Value("${security.expiration}")
    private int jetExpirationMs;

    private final ResetTokenStore resetTokenStore;

    public String generateJwtToken(Authentication authentication) {
        String username = authentication.getName();
        String userType = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        HasId user = (HasId) authentication.getPrincipal();
        UUID userId = user.getId();
        Date now = new Date();
        Date exp = new Date(now.getTime() + jetExpirationMs);
        return Jwts.builder()
                .setSubject(username)
                .claim("userType", userType)
                .claim("userId", userId.toString())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jetSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public <T> T getClaimFromJwtToken(String token, String claimName, Class<T> clazz) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
        return claims.get(claimName, clazz);
    }

    public String generatePasswordResetToken(String userIdStr) {
        Date now = new Date();
        int resetExpirationMs = 1200000;

        Date exp = new Date(now.getTime() + resetExpirationMs);
        return Jwts.builder()
                .setSubject(userIdStr)
                .setAudience("user-service")
                .claim("typ", "reset_password")
                .setIssuedAt(now)
                .setExpiration(exp)
                .setId(UUID.randomUUID().toString())
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateResetToken(String token) {
        try {
            Claims c = Jwts.parserBuilder().setSigningKey(key()).build()
                    .parseClaimsJws(token).getBody();
            boolean okType = "reset_password".equals(c.get("typ"));
            boolean okAud  = "user-service".equals(c.getAudience());
            boolean okExp  = c.getExpiration() != null && c.getExpiration().after(new Date());
            boolean okJti  = c.getId() != null && !resetTokenStore.isConsumed(c.getId());
            return okType && okAud && okExp && okJti;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("validateResetToken failed: {}", e.getMessage());
            return false;
        }
    }

    public UUID subjectAsUserId(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
        return UUID.fromString(c.getSubject());
    }

    public String getJti(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
        return c.getId();
    }

    public Date getExpiration(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
        return c.getExpiration();
    }

    public boolean consumeResetToken(String token) {
        String jti = getJti(token);
        Date exp = getExpiration(token);
        long remainMs = Math.max(0, exp.getTime() - System.currentTimeMillis());
        return resetTokenStore.consumeOnce(jti, Duration.ofMillis(remainMs));
    }
}
