package com.basespringboot.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtService {

    private final byte[] secretKey;
    private final long expirationSeconds;

    public JwtService(
            @Value("${jwt.secret:SuperSecretRailwayTokenKeyWithHighEntropyAtLeast256BitsLong!}") String secret,
            @Value("${jwt.expiration-seconds:86400}") long expirationSeconds
    ) {
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(Long userId, String username, String role, String fullName) {
        long now = Instant.now().getEpochSecond();
        long exp = now + expirationSeconds;

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format(
                "{\"sub\":\"%s\",\"userId\":%d,\"role\":\"%s\",\"fullName\":\"%s\",\"iat\":%d,\"exp\":%d}",
                escapeJson(username),
                userId,
                escapeJson(role),
                escapeJson(fullName != null ? fullName : ""),
                now,
                exp
        );

        String encodedHeader = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String dataToSign = encodedHeader + "." + encodedPayload;
        String signature = sign(dataToSign);

        return dataToSign + "." + signature;
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        String dataToSign = parts[0] + "." + parts[1];
        String expectedSignature = sign(dataToSign);

        if (!MessageDigest.isEqual(parts[2].getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
            return false;
        }

        long exp = extractExp(parts[1]);
        return exp > Instant.now().getEpochSecond();
    }

    public Map<String, Object> parseClaims(String token) {
        if (!validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired JWT token");
        }
        String[] parts = token.split("\\.");
        String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
        return parseSimpleJson(payloadJson);
    }

    public String extractUsername(String token) {
        Map<String, Object> claims = parseClaims(token);
        return (String) claims.get("sub");
    }

    public String extractRole(String token) {
        Map<String, Object> claims = parseClaims(token);
        return (String) claims.get("role");
    }

    public Long extractUserId(String token) {
        Map<String, Object> claims = parseClaims(token);
        Object id = claims.get("userId");
        if (id instanceof Number n) {
            return n.longValue();
        }
        return id != null ? Long.parseLong(id.toString()) : null;
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] signed = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(signed);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Error signing JWT", e);
        }
    }

    private long extractExp(String encodedPayload) {
        try {
            String json = new String(base64UrlDecode(encodedPayload), StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("\"exp\":\\s*(\\d+)");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }

    private Map<String, Object> parseSimpleJson(String json) {
        Map<String, Object> map = new HashMap<>();
        Pattern stringPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");
        Matcher stringMatcher = stringPattern.matcher(json);
        while (stringMatcher.find()) {
            map.put(stringMatcher.group(1), stringMatcher.group(2));
        }

        Pattern numberPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\\d+)");
        Matcher numberMatcher = numberPattern.matcher(json);
        while (numberMatcher.find()) {
            map.putIfAbsent(numberMatcher.group(1), Long.parseLong(numberMatcher.group(2)));
        }
        return map;
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
