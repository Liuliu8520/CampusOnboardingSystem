package com.campus.onboarding.security;

import com.campus.onboarding.common.BizException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TokenUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-hours}")
    private long expirationHours;

    public String generate(String account, String role, String displayName) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", account);
            payload.put("role", role);
            payload.put("name", displayName);
            payload.put("exp", Instant.now().plusSeconds(expirationHours * 3600).getEpochSecond());

            String encodedHeader = base64Url(OBJECT_MAPPER.writeValueAsBytes(header));
            String encodedPayload = base64Url(OBJECT_MAPPER.writeValueAsBytes(payload));
            String unsigned = encodedHeader + "." + encodedPayload;
            return unsigned + "." + sign(unsigned);
        } catch (Exception ex) {
            throw new BizException(500, "令牌生成失败");
        }
    }

    public AuthUser parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BizException(401, "令牌格式错误");
            }
            String unsigned = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(unsigned), parts[2])) {
                throw new BizException(401, "令牌签名无效");
            }
            Map<String, Object> payload = OBJECT_MAPPER.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<>() {
                    }
            );
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > exp) {
                throw new BizException(401, "登录已过期");
            }
            return new AuthUser(
                    String.valueOf(payload.get("sub")),
                    String.valueOf(payload.get("role")),
                    String.valueOf(payload.get("name"))
            );
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(401, "登录已失效，请重新登录");
        }
    }

    private String sign(String unsigned) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return base64Url(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] left = a.getBytes(StandardCharsets.UTF_8);
        byte[] right = b.getBytes(StandardCharsets.UTF_8);
        if (left.length != right.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length; i++) {
            result |= left[i] ^ right[i];
        }
        return result == 0;
    }
}
