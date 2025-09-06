package test.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.enums.Status;
import main.http.HttpTaskServer;
import main.manager.Managers;
import main.manager.TaskManager;
import main.models.Task;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrioritizedHandlerTest {
    private static HttpTaskServer server;
    private static TaskManager manager;
    private static Gson gson;
    private static HttpClient client;

    @BeforeAll
    static void startServer() throws Exception {
        manager = Managers.getDefault();
        server = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() {
        manager.deleteAllTasks();
    }

    @Test
    void testGetPrioritizedTasks() throws Exception {
        Task task1 = manager.createTask(new Task("Task 1", "desc", 1, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15)));
        Task task2 = manager.createTask(new Task("Task 2", "desc", 2, Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {}.getType());
        assertEquals(List.of(task1, task2), tasks);
    }
}
