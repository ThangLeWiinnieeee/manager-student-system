package vn.edu.fit.topicmanagement.auth;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RecoveryMailSender {
    private final JavaMailSender smtp;
    private final RestClient api;
    private final GmailApiMailSender gmail;

    @Autowired
    public RecoveryMailSender(JavaMailSender smtp, @Value("${BREVO_API_KEY:}") String key, GmailApiMailSender gmail) {
        this(smtp, key, "https://api.brevo.com/v3/smtp/email", gmail);
    }

    RecoveryMailSender(JavaMailSender smtp, String key, String endpoint, GmailApiMailSender gmail) {
        this.smtp = smtp;
        this.gmail = gmail;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        api = key.isBlank() ? null : RestClient.builder().requestFactory(factory)
                .baseUrl(endpoint).defaultHeader("api-key", key).build();
    }

    public void send(SimpleMailMessage message) {
        if (gmail.isConfigured()) {
            gmail.send(message);
            return;
        }
        if (api == null) {
            smtp.send(message);
            return;
        }
        try {
            api.post().contentType(MediaType.APPLICATION_JSON).body(Map.of(
                    "sender", Map.of("email", message.getFrom()),
                    "to", List.of(Map.of("email", message.getTo()[0])),
                    "subject", message.getSubject(), "textContent", message.getText()))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException ex) {
            // Do not include provider responses containing recipients or credentials in logs.
            throw new MailSendException("Email API delivery could not be confirmed");
        }
    }
}
