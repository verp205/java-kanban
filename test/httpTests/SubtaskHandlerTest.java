package httpTests;

import com.google.gson.Gson;
import main.enums.Status;
import main.http.HttpTaskServer;
import main.manager.Managers;
import main.manager.TaskManager;
import main.models.Epic;
import main.models.Subtask;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskHandlerTest {
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
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
    }

    @Test
    void testGetAllSubtasksEmpty() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void testCreateSubtask() throws Exception {
        Epic epic = manager.createEpic(new Epic("Epic 1", "desc", 0, Status.NEW));
        Subtask subtask = new Subtask("Subtask 1", "desc", 0, Status.NEW,
                epic.getId(), LocalDateTime.now(), Duration.ofMinutes(30));
        String body = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        // Сначала удаляем все сабтаски, чтобы избежать пересечений
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .DELETE()
                .build();
        client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        // Создаём эпик
        Epic epic = new Epic("Epic 1", "desc", 0, Status.NEW);
        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();
        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        Epic createdEpic = gson.fromJson(epicResponse.body(), Epic.class);

        // Создаём сабтаск
        Subtask subtask = new Subtask(
                "Subtask 1",
                "desc",
                0,
                Status.NEW,
                createdEpic.getId(),
                LocalDateTime.now().plusHours(1), // безопасное время
                Duration.ofMinutes(30)
        );

        HttpRequest subtaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();
        HttpResponse<String> subtaskResponse = client.send(subtaskRequest, HttpResponse.BodyHandlers.ofString());
        Subtask createdSubtask = gson.fromJson(subtaskResponse.body(), Subtask.class);

        // Обновляем сабтаск через PUT
        createdSubtask.setName("Updated Subtask");
        createdSubtask.setStatus(Status.DONE);
        createdSubtask.setStartTime(LocalDateTime.now().plusHours(2)); // новое безопасное время
        createdSubtask.setDuration(Duration.ofMinutes(45));

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(createdSubtask)))
                .build();
        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа и обновлённые данные
        assertEquals(200, updateResponse.statusCode());

        Subtask updatedSubtask = gson.fromJson(updateResponse.body(), Subtask.class);
        assertEquals("Updated Subtask", updatedSubtask.getName());
        assertEquals(Status.DONE, updatedSubtask.getStatus());
        assertEquals(createdSubtask.getStartTime(), updatedSubtask.getStartTime());
        assertEquals(createdSubtask.getDuration(), updatedSubtask.getDuration());
    }

    @Test
    void testGetSubtaskByIdNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/subtasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void testDeleteSubtaskNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/subtasks/999"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode());
    }
}
