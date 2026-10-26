package vn.edu.fit.topicmanagement.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GmailApiMailSender {
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String tokenUrl;
    private final String sendUrl;
    private final RestClient client;

    @Autowired
    public GmailApiMailSender(@Value("${GMAIL_CLIENT_ID:}") String clientId,
            @Value("${GMAIL_CLIENT_SECRET:}") String clientSecret,
            @Value("${GMAIL_REFRESH_TOKEN:}") String refreshToken) {
        this(clientId, clientSecret, refreshToken, "https://oauth2.googleapis.com/token",
                "https://gmail.googleapis.com/gmail/v1/users/me/messages/send");
    }

    GmailApiMailSender(String clientId, String clientSecret, String refreshToken, String tokenUrl, String sendUrl) {
        this.clientId = clientId.trim();
        this.clientSecret = clientSecret.trim();
        this.refreshToken = refreshToken.trim();
        boolean any = !this.clientId.isEmpty() || !this.clientSecret.isEmpty() || !this.refreshToken.isEmpty();
        if (any && (this.clientId.isEmpty() || this.clientSecret.isEmpty() || this.refreshToken.isEmpty())) {
            throw new IllegalArgumentException("Configure GMAIL_CLIENT_ID, GMAIL_CLIENT_SECRET and GMAIL_REFRESH_TOKEN together");
        }
        this.tokenUrl = tokenUrl;
        this.sendUrl = sendUrl;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        client = RestClient.builder().requestFactory(factory).build();
    }

    public boolean isConfigured() { return !clientId.isEmpty(); }

    public void send(SimpleMailMessage message) {
        if (!isConfigured()) throw new MailSendException("Gmail API is not configured");
        try {
            var mime = new MimeMessage(Session.getInstance(new Properties()));
            var helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setFrom(message.getFrom());
            helper.setTo(message.getTo());
            helper.setSubject(message.getSubject());
            helper.setText(message.getText(), false);
            mime.saveChanges();
            var bytes = new ByteArrayOutputStream();
            mime.writeTo(bytes);

            var form = new LinkedMultiValueMap<String, String>();
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
            form.add("refresh_token", refreshToken);
            form.add("grant_type", "refresh_token");
            // shortcut: refresh per OTP; cache access tokens if email volume grows.
            var token = client.post().uri(tokenUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(com.fasterxml.jackson.databind.JsonNode.class);
            if (token == null || !token.path("access_token").isTextual()
                    || token.path("access_token").asText().isBlank()) {
                throw new MailSendException("Gmail API did not return an access token");
            }
            client.post().uri(sendUrl).headers(h -> h.setBearerAuth(token.path("access_token").asText()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("raw", Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray())))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException | MessagingException | IOException ex) {
            // Provider bodies and exception causes may contain tokens, recipients or OTPs.
            throw new MailSendException("Gmail API delivery could not be confirmed");
        }
    }
}
