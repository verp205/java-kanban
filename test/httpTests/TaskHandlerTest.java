package test.http;

import com.google.gson.Gson;
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

import static org.junit.jupiter.api.Assertions.*;

class TaskHandlerTest {
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
    void testGetAllTasksEmpty() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void testGetTaskById() throws Exception {
        Task task = new Task("Task 1", "desc", 1, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        manager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/tasks/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task fromJson = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), fromJson.getId());
    }

    @Test
    void testDeleteTask() throws Exception {
        Task task = new Task("Task 2", "desc", 2, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/tasks/2"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode());
        assertNull(manager.getTask(2));
    }
}
