package vn.edu.fit.topicmanagement.submission;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;
import static org.junit.jupiter.api.Assertions.*;

class ReportStorageTest {
    @TempDir Path directory;
    private static final String NAME = "12345678-1234-1234-1234-123456789abc.pdf";
    private final MockMultipartFile pdf = new MockMultipartFile("file", "report.pdf", "application/pdf", "%PDF-test".getBytes());

    @Test void localStorageRetainsFilesAndRejectsTraversal() throws Exception {
        var storage = new ReportStorage(directory.toString(), "", "", "reports");
        storage.save(NAME, pdf);
        assertArrayEquals(pdf.getBytes(), storage.read(NAME).getContentAsByteArray());
        assertThrows(ResourceNotFoundException.class, () -> storage.read("../secret.pdf"));
        storage.delete(NAME);
        assertThrows(ResourceNotFoundException.class, () -> storage.read(NAME));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"pdf", "zip"})
    void remoteStorageUsesAuthenticatedObjectsAndHandlesFailure(String extension) throws Exception {
        String name = NAME.replace(".pdf", "." + extension);
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var content = new AtomicReference<byte[]>();
        var authorization = new AtomicReference<String>();
        var contentType = new AtomicReference<String>();
        server.createContext("/storage/v1/object/reports/" + name, exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            switch (exchange.getRequestMethod()) {
                case "POST" -> { contentType.set(exchange.getRequestHeaders().getFirst("Content-Type")); content.set(exchange.getRequestBody().readAllBytes()); exchange.sendResponseHeaders(200, -1); }
                case "DELETE" -> { content.set(null); exchange.sendResponseHeaders(200, -1); }
                default -> {
                    byte[] bytes = content.get();
                    if (bytes == null) exchange.sendResponseHeaders(404, -1);
                    else { exchange.sendResponseHeaders(200, bytes.length); exchange.getResponseBody().write(bytes); }
                }
            }
            exchange.close();
        });
        server.start();
        try {
            var storage = new ReportStorage(directory.toString(), "http://127.0.0.1:" + server.getAddress().getPort(), "test-key", "reports");
            storage.save(name, pdf);
            assertArrayEquals(pdf.getBytes(), storage.read(name).getContentAsByteArray());
            assertEquals("application/" + extension, contentType.get());
            assertEquals("Bearer test-key", authorization.get());
            storage.delete(name);
            assertThrows(BusinessRuleException.class, () -> storage.read(name));
        } finally { server.stop(0); }
    }
}
