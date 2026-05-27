package com.womensocial.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:40px 32px;background:#fff;border-radius:16px;">
                  <h2 style="color:#1a1a1a;margin:0 0 8px;">Your Gul sign-in code</h2>
                  <p style="color:#666;font-size:14px;margin:0 0 28px;">Use this code to sign in or create your account. It expires in 5 minutes.</p>
                  <div style="background:#f5f5f5;border-radius:12px;padding:28px;text-align:center;margin-bottom:28px;">
                    <span style="font-size:40px;font-weight:900;letter-spacing:12px;color:#1a1a1a;">%s</span>
                  </div>
                  <p style="color:#aaa;font-size:12px;text-align:center;margin:0;">If you didn't request this code, you can safely ignore this email.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
                  <p style="color:#ccc;font-size:11px;text-align:center;margin:0;">Gul — a safe space for women 💜</p>
                </div>
                """.formatted(otpCode);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Your Gul verification code: " + otpCode);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("OTP email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String displayName, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:32px;background:#fff;">
                  <h2 style="color:#e91e8c;">Reset your Sparkle password</h2>
                  <p>Hi %s,</p>
                  <p>We received a request to reset your password. Click the button below to choose a new one:</p>
                  <div style="text-align:center;margin:32px 0;">
                    <a href="%s"
                       style="background:#e91e8c;color:#fff;padding:14px 32px;border-radius:8px;
                              text-decoration:none;font-weight:bold;font-size:16px;">
                      Reset Password
                    </a>
                  </div>
                  <p style="color:#888;font-size:13px;">This link expires in 30 minutes.</p>
                  <p style="color:#888;font-size:13px;">If you didn't request this, you can safely ignore this email.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
                  <p style="color:#bbb;font-size:12px;text-align:center;">Sparkle — a safe space for women 💜</p>
                </div>
                """.formatted(displayName, resetLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Reset your Sparkle password");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}
