package com.foodorder.usermanagement.mocks;

import com.foodorder.usermanagement.model.User;
import org.mockito.Mockito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DependencyMocks {

    // Database Connection Mock
    public static class DatabaseConnectionMock {
        private boolean isConnected = true;
        private List<User> users = new ArrayList<>();

        public void simulateConnectionFailure() {
            isConnected = false;
        }

        public void simulateConnectionRecovery() {
            isConnected = true;
        }

        public boolean isConnected() {
            return isConnected;
        }

        public void addUser(User user) {
            if (isConnected) {
                users.add(user);
            }
        }

        public Optional<User> findUserByEmail(String email) {
            if (!isConnected) {
                throw new RuntimeException("Database connection failed");
            }
            return users.stream()
                    .filter(user -> user.getEmail().equals(email))
                    .findFirst();
        }
    }

    // External API Mock
    public static class ExternalAPIMock {
        private final RestTemplate restTemplate;
        private boolean isAvailable = true;
        private int responseDelay = 0;

        public ExternalAPIMock() {
            this.restTemplate = Mockito.mock(RestTemplate.class);
        }

        public void simulateAPIFailure() {
            isAvailable = false;
        }

        public void simulateAPIRecovery() {
            isAvailable = true;
        }

        public void setResponseDelay(int milliseconds) {
            this.responseDelay = milliseconds;
        }

        public <T> T makeAPICall(String url, Class<T> responseType) {
            if (!isAvailable) {
                throw new RuntimeException("External API is not available");
            }
            try {
                Thread.sleep(responseDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return Mockito.mock(responseType);
        }
    }

    // File System Mock
    public static class FileSystemMock {
        private boolean isWritable = true;
        private List<Path> createdFiles = new ArrayList<>();

        public void simulateWriteFailure() {
            isWritable = false;
        }

        public void simulateWriteRecovery() {
            isWritable = true;
        }

        public void createFile(Path path, String content) throws IOException {
            if (!isWritable) {
                throw new IOException("File system is not writable");
            }
            Files.write(path, content.getBytes());
            createdFiles.add(path);
        }

        public String readFile(Path path) throws IOException {
            if (!Files.exists(path)) {
                throw new IOException("File does not exist");
            }
            return new String(Files.readAllBytes(path));
        }

        public void deleteFile(Path path) throws IOException {
            if (!isWritable) {
                throw new IOException("File system is not writable");
            }
            Files.delete(path);
            createdFiles.remove(path);
        }
    }

    // Email Service Mock
    public static class EmailServiceMock {
        private final JavaMailSender mailSender;
        private boolean isAvailable = true;
        private List<String> sentEmails = new ArrayList<>();

        public EmailServiceMock() {
            this.mailSender = Mockito.mock(JavaMailSender.class);
        }

        public void simulateEmailFailure() {
            isAvailable = false;
        }

        public void simulateEmailRecovery() {
            isAvailable = true;
        }

        public void sendEmail(String to, String subject, String body) {
            if (!isAvailable) {
                throw new RuntimeException("Email service is not available");
            }
            sentEmails.add(String.format("To: %s, Subject: %s, Body: %s", to, subject, body));
        }

        public List<String> getSentEmails() {
            return new ArrayList<>(sentEmails);
        }
    }

    // Example usage in tests:
    /*
    @Test
    void testDatabaseConnectionFailure() {
        DatabaseConnectionMock dbMock = new DatabaseConnectionMock();
        dbMock.simulateConnectionFailure();
        assertThrows(RuntimeException.class, () -> 
            dbMock.findUserByEmail("test@example.com"));
    }

    @Test
    void testExternalAPITimeout() {
        ExternalAPIMock apiMock = new ExternalAPIMock();
        apiMock.setResponseDelay(5000); // 5 seconds delay
        assertTimeout(Duration.ofSeconds(6), () -> 
            apiMock.makeAPICall("http://api.example.com", String.class));
    }

    @Test
    void testFileSystemWriteFailure() {
        FileSystemMock fsMock = new FileSystemMock();
        fsMock.simulateWriteFailure();
        assertThrows(IOException.class, () -> 
            fsMock.createFile(Path.of("test.txt"), "content"));
    }

    @Test
    void testEmailServiceFailure() {
        EmailServiceMock emailMock = new EmailServiceMock();
        emailMock.simulateEmailFailure();
        assertThrows(RuntimeException.class, () -> 
            emailMock.sendEmail("test@example.com", "Subject", "Body"));
    }
    */
} 