package vn.edu.fit.topicmanagement.submission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;

/** Storage only; callers must check group access before reading a report. */
@Component
public class ReportStorage {
    private static final Logger log = LoggerFactory.getLogger(ReportStorage.class);
    private final Path root;
    private final RestClient remote;

    public ReportStorage(@Value("${app.submission.storage-dir}") String directory,
            @Value("${SUPABASE_URL:}") String url,
            @Value("${SUPABASE_SERVICE_ROLE_KEY:}") String key,
            @Value("${SUPABASE_STORAGE_BUCKET:reports}") String bucket,
            @Value("${app.submission.require-remote:false}") boolean requireRemote) {
        url = url.trim();
        key = key.trim();
        bucket = bucket.trim();
        root = Path.of(directory).toAbsolutePath().normalize();
        if (requireRemote && (url.isBlank() || key.isBlank())) {
            throw new IllegalArgumentException("Hosted reports require SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY");
        }
        if (url.isBlank() != key.isBlank()) {
            throw new IllegalArgumentException("Configure both Supabase URL and service-role key");
        }
        if (!bucket.matches("[a-zA-Z0-9_-]+")) throw new IllegalArgumentException("Invalid storage bucket");
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
        remote = url.isBlank() ? null : RestClient.builder().requestFactory(factory)
                .baseUrl(url.replaceAll("/+$", "") + "/storage/v1/object/" + bucket)
                .defaultHeader("apikey", key).defaultHeader("Authorization", "Bearer " + key).build();
        if (remote == null) log.info("Report storage: local directory {}", root);
        else log.info("Report storage: Supabase bucket {}", bucket);
    }

    public void save(String name, MultipartFile file) {
        Path path = path(name);
        try {
            if (remote == null) {
                Files.createDirectories(root);
                try (var input = file.getInputStream()) { Files.copy(input, path); }
            } else {
                remote.post().uri("/{name}", name).contentType(name.endsWith(".zip")
                        ? MediaType.parseMediaType("application/zip") : MediaType.APPLICATION_PDF)
                        .body(file.getBytes()).retrieve().toBodilessEntity();
            }
        } catch (IOException | RestClientException ex) {
            if (remote == null) log.error("Cannot save report to {}", path, ex);
            else log.error("Cannot save report to remote storage ({})", ex.getClass().getSimpleName());
            throw new BusinessRuleException("Không thể lưu tệp báo cáo. Vui lòng thử lại.");
        }
    }

    public Resource read(String name) {
        Path path = path(name);
        if (remote == null) {
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                throw new ResourceNotFoundException("Tệp báo cáo không tồn tại");
            }
            return new FileSystemResource(path);
        }
        try {
            // shortcut: reports are limited to 10 MB; stream downloads if concurrency grows.
            byte[] bytes = remote.get().uri("/{name}", name).retrieve().body(byte[].class);
            if (bytes == null) throw new BusinessRuleException("Tệp báo cáo trống");
            return new ByteArrayResource(bytes);
        } catch (RestClientException ex) {
            throw new BusinessRuleException("Không thể tải tệp báo cáo. Vui lòng thử lại.");
        }
    }

    public void delete(String name) {
        Path path = path(name);
        try {
            if (remote == null) Files.deleteIfExists(path);
            else remote.delete().uri("/{name}", name).retrieve().toBodilessEntity();
        } catch (IOException | RestClientException ex) {
            throw new BusinessRuleException("Không thể dọn tệp báo cáo");
        }
    }

    private Path path(String name) {
        if (name == null || !name.matches("[0-9a-fA-F-]{36}\\.(pdf|zip)")) {
            throw new ResourceNotFoundException("Tên tệp lưu trữ không hợp lệ");
        }
        return root.resolve(name);
    }
}
