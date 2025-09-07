package httpTests;

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
    void testCreateTask() throws Exception {
        Task task = new Task("Task 1", "desc", 0, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        String body = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        Task created = gson.fromJson(response.body(), Task.class);
        assertNotEquals(0, created.getId());
        assertEquals("Task 1", created.getName());
    }

    @Test
    void testUpdateTask() throws Exception {
        Task task = manager.createTask(new Task("Task", "desc", 1, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        task.setName("Updated Task");
        String body = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task updated = gson.fromJson(response.body(), Task.class);
        assertEquals("Updated Task", updated.getName());
    }

    @Test
    void testGetTaskByIdNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/tasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("не найдена"));
    }

    @Test
    void testDeleteTaskNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/tasks/999"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode());
    }

    @Test
    void testInvalidJsonInPost() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString("{bad json}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode());
    }
}
