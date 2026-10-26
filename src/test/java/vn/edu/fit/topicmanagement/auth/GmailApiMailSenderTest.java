package vn.edu.fit.topicmanagement.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GmailApiMailSenderTest {
    @Test void refreshesTokenAndSendsUtf8MimeWithoutFallbackOnFailure() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var tokenStatus = new AtomicInteger(200);
        var sendStatus = new AtomicInteger(200);
        var tokenResponse = new AtomicReference<>("{\"access_token\":\"access-test\"}");
        var tokenForm = new AtomicReference<String>();
        var auth = new AtomicReference<String>();
        var raw = new AtomicReference<String>();
        var sends = new AtomicInteger();
        server.createContext("/token", exchange -> {
            tokenForm.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = tokenResponse.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(tokenStatus.get(), response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.createContext("/send", exchange -> {
            sends.incrementAndGet();
            auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            raw.set(new ObjectMapper().readTree(exchange.getRequestBody()).get("raw").asText());
            exchange.sendResponseHeaders(sendStatus.get(), -1);
            exchange.close();
        });
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            var gmail = new GmailApiMailSender("client", "secret+&", "refresh", base + "/token", base + "/send");
            var smtp = mock(JavaMailSender.class);
            var sender = new RecoveryMailSender(smtp, "unused-brevo", base + "/must-not-call", gmail);
            var message = new SimpleMailMessage();
            message.setFrom("sender@example.test");
            message.setTo("student@example.test");
            message.setSubject("Mã xác minh mật khẩu");
            message.setText("Mã OTP của bạn: 123456");
            sender.send(message);
            assertTrue(tokenForm.get().contains("client_secret=secret%2B%26"));
            assertTrue(tokenForm.get().contains("grant_type=refresh_token"));
            assertTrue(tokenForm.get().contains("refresh_token=refresh"));
            assertEquals("Bearer access-test", auth.get());
            var mime = new MimeMessage(Session.getInstance(new Properties()),
                    new ByteArrayInputStream(Base64.getUrlDecoder().decode(raw.get())));
            assertEquals(message.getSubject(), mime.getSubject());
            assertEquals(message.getText(), mime.getContent());
            assertEquals("student@example.test", mime.getAllRecipients()[0].toString());
            assertEquals("sender@example.test", mime.getFrom()[0].toString());
            sendStatus.set(503);
            assertThrows(MailSendException.class, () -> sender.send(message));
            assertEquals(2, sends.get());
            tokenStatus.set(400);
            tokenResponse.set("{\"error\":\"invalid_grant\",\"secret\":\"do-not-log\"}");
            var failure = assertThrows(MailSendException.class, () -> sender.send(message));
            assertNull(failure.getCause());
            assertFalse(failure.getMessage().contains("do-not-log"));
            tokenStatus.set(200);
            tokenResponse.set("{}");
            assertThrows(MailSendException.class, () -> sender.send(message));
            assertEquals(2, sends.get());
            verifyNoInteractions(smtp);
        } finally { server.stop(0); }
    }

    @Test void rejectsPartialConfigurationAndKeepsLocalSmtp() {
        assertThrows(IllegalArgumentException.class, () -> new GmailApiMailSender("id", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new GmailApiMailSender("", "secret", "refresh"));
        var gmail = new GmailApiMailSender("", "", "");
        assertFalse(gmail.isConfigured());
        var smtp = mock(JavaMailSender.class);
        var message = new SimpleMailMessage();
        new RecoveryMailSender(smtp, "", gmail).send(message);
        verify(smtp).send(message);
    }
}
