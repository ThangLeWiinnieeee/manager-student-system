package vn.edu.fit.topicmanagement.auth;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecoveryMailSenderTest {
    @Test void sendsJsonOverHttpAndConvertsFailuresToMailExceptions() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var body = new AtomicReference<String>();
        var key = new AtomicReference<String>();
        var status = new AtomicInteger(201);
        server.createContext("/email", exchange -> {
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            key.set(exchange.getRequestHeaders().getFirst("api-key"));
            exchange.sendResponseHeaders(status.get(), -1);
            exchange.close();
        });
        server.start();
        try {
            var smtp = mock(JavaMailSender.class);
            var sender = new RecoveryMailSender(smtp, "test-key", "http://127.0.0.1:" + server.getAddress().getPort() + "/email");
            var message = new SimpleMailMessage();
            message.setFrom("sender@example.test");
            message.setTo("student@example.test");
            message.setSubject("OTP");
            message.setText("Code: 123456");
            sender.send(message);
            var json = new ObjectMapper().readTree(body.get());
            assertEquals("student@example.test", json.get("to").get(0).get("email").asText());
            assertEquals("Code: 123456", json.get("textContent").asText());
            assertEquals("test-key", key.get());
            verifyNoInteractions(smtp);
            status.set(503);
            assertThrows(MailSendException.class, () -> sender.send(message));
        } finally { server.stop(0); }
    }
}
