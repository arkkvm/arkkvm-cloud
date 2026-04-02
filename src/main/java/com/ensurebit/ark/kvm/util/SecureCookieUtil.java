package com.ensurebit.ark.kvm.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class SecureCookieUtil {

    private static final String HMAC_ALGORITHM = "HMACSHA256";
    private static final String ENCODING = "UTF-8";

    @Value("${oauth2.cookie.signing-key}")
    private String signingKey;

    @Value("${oauth2.cookie.max-age:600}")
    private int defaultMaxAge;

    @Value("${oauth2.cookie.secure:false}")
    private boolean secure;

    public void setCookie(HttpServletResponse response, String name, String value) {
        setCookie(response, name, value, null);
    }

    public void setCookie(
            HttpServletResponse response,
            String name,
            String value,
            Integer maxAge
    ) {
        if (value == null || name == null) return;

        int age = (maxAge != null && maxAge > 0) ? maxAge : defaultMaxAge;

        try {

            String signature = calculateHmac(value);
            String signedValue = URLEncoder.encode(value, ENCODING) + "." + signature;


            Cookie cookie = new Cookie(name, signedValue);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            if (secure) cookie.setSecure(true);
            cookie.setMaxAge(age);

            response.addCookie(cookie);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set signed cookie: " + name, e);
        }
    }


    public String getCookieValue(
            HttpServletRequest request,
            String name
    ) {
        Cookie cookie = WebUtils.getCookie(request, name);

        if (cookie == null) {
            return null;
        }

        String signedValue = cookie.getValue();
        if (signedValue == null || !signedValue.contains(".")) {
            return null;
        }

        try {
            int lastDotIndex = signedValue.lastIndexOf('.');
            String encodedValue = signedValue.substring(0, lastDotIndex);
            String signature = signedValue.substring(lastDotIndex + 1);


            String rawValue = URLDecoder.decode(encodedValue, String.valueOf(StandardCharsets.UTF_8));


            String expectedSignature = calculateHmac(rawValue);
            if (MessageDigest.isEqual(signature.getBytes(), expectedSignature.getBytes())) {
                return rawValue;
            } else {
                System.out.println("Signature mismatch for cookie: " + name);
            }
        } catch (Exception e) {
            System.out.println("Failed to parse or verify cookie: " + name);
        }
        return null;
    }


    public void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        if (secure) cookie.setSecure(true);
        response.addCookie(cookie);
    }


    private String calculateHmac(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(keySpec);
        byte[] digest = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest);
    }


    private static boolean isEqual(byte[] a, byte[] b) {
        return java.security.MessageDigest.isEqual(a, b);
    }
}