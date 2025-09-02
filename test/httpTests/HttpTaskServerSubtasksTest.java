package httpTests;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import main.enums.Status;
import main.http.HttpTaskServer;
import main.manager.InMemoryTaskManager;
import main.manager.TaskManager;
import main.models.Epic;
import main.models.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerSubtasksTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    @Test
    void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic with subtasks", "Epic desc", 0, Status.NEW);
        manager.createEpic(epic);

        Subtask subtask = new Subtask(
                "Sub 1",
                "Do it",
                0,
                Status.NEW,
                epic.getId(),
                LocalDateTime.now().plusHours(1), // избегаем пересечения
                Duration.ofMinutes(30)
        );

        String json = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Subtask должен быть создан с 201");

        // Проверяем, что subtask добавился в менеджер
        List<Subtask> subtasks = manager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("Sub 1", subtasks.get(0).getName());
        assertEquals(epic.getId(), subtasks.get(0).getEpicId());
    }
}
