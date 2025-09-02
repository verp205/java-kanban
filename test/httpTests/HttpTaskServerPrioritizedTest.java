package httpTests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.http.HttpTaskServer;
import main.manager.InMemoryTaskManager;
import main.models.Task;
import main.enums.Status;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpTaskServerPrioritizedTest {

    private HttpTaskServer server;
    private HttpClient client;
    private Gson gson;

    @BeforeAll
    void startServer() throws IOException {
        server = new HttpTaskServer(new InMemoryTaskManager());
        server.start();
        client = HttpClient.newHttpClient();
        gson = HttpTaskServer.getGson();
    }

    @AfterAll
    void stopServer() {
        server.stop();
    }

    @Test
    void testCreateTasksAndGetPrioritized() throws IOException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task1", "Desc1", 0, Status.NEW, now, Duration.ofHours(1));
        Task task2 = new Task("Task2", "Desc2", 0, Status.NEW, now.plusHours(2), Duration.ofHours(2)); // +2 часа
        Task task3 = new Task("Task3", "Desc3", 0, Status.NEW, now.plusHours(5), Duration.ofHours(1)); // +5 часов


        createTask(task1);
        createTask(task2);
        createTask(task3);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(3, tasks.size());
        assertEquals("Task1", tasks.get(0).getName());
        assertEquals("Task2", tasks.get(1).getName());
        assertEquals("Task3", tasks.get(2).getName());
    }

    private void createTask(Task task) throws IOException, InterruptedException {
        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
    }
}
