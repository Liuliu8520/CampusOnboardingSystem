package com.campus.onboarding.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务。
 *
 * <p>当前为数学公式验证码实现（如 {@code 10 - 7 = ?}）：generate 生成题目并以 captchaId 为键
 * 将答案暂存于内存，verify 校验成功后即失效（一次性）。
 *
 * <p>该结构预留了未来升级为 Kaptcha 图形验证码的扩展点：只需在 generate 中替换为图片生成
 * （图片字节 Base64 返回前端，文本答案按 captchaId 存储），verify 逻辑保持不变。
 * 若后续部署多实例，可将 store 替换为 Redis 等共享缓存。
 */
@Service
public class CaptchaService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final String[] OPERATORS = {"+", "-", "×"};

    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    /**
     * 生成一道数学公式验证码，返回 captchaId 与题目文本。
     *
     * @return 包含 captchaId、question 两个键的 Map
     */
    public Map<String, String> generate() {
        purgeExpired();
        int a = RANDOM.nextInt(20) + 1;
        int b = RANDOM.nextInt(20) + 1;
        String operator = OPERATORS[RANDOM.nextInt(OPERATORS.length)];
        int left;
        int right;
        int answer;
        switch (operator) {
            case "-" -> {
                // 减法保证结果非负，便于心算
                if (a >= b) {
                    left = a;
                    right = b;
                } else {
                    left = b;
                    right = a;
                }
                answer = left - right;
            }
            case "×" -> {
                // 乘法使用较小操作数，避免结果过大
                left = RANDOM.nextInt(9) + 1;
                right = RANDOM.nextInt(9) + 1;
                answer = left * right;
            }
            default -> {
                left = a;
                right = b;
                answer = left + right;
            }
        }
        String question = left + " " + operator + " " + right + " = ?";
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        store.put(captchaId, new CaptchaEntry(String.valueOf(answer), Instant.now().plus(TTL)));
        Map<String, String> result = new LinkedHashMap<>();
        result.put("captchaId", captchaId);
        result.put("question", question);
        return result;
    }

    /**
     * 校验用户输入是否匹配指定 captchaId 对应的答案，校验后无论成功与否都移除该条记录，
     * 避免被重复使用。
     *
     * @param captchaId generate 返回的 captchaId
     * @param answer   用户输入的答案
     * @return 正确返回 true，captchaId 不存在、已过期或答案错误返回 false
     */
    public boolean verify(String captchaId, String answer) {
        if (captchaId == null || answer == null) {
            return false;
        }
        CaptchaEntry entry = store.remove(captchaId);
        if (entry == null || Instant.now().isAfter(entry.expireAt())) {
            return false;
        }
        return entry.answer().equalsIgnoreCase(answer.trim());
    }

    /** 惰性清理已过期条目，避免内存泄漏。 */
    private void purgeExpired() {
        Instant now = Instant.now();
        store.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expireAt()));
    }

    private record CaptchaEntry(String answer, Instant expireAt) {
    }
}
