package httpTests;

import com.google.gson.Gson;
import main.enums.Status;
import main.http.HttpTaskServer;
import main.manager.Managers;
import main.manager.TaskManager;
import main.models.Epic;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class EpicHandlerTest {
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
        manager.deleteAllEpics();
    }

    @Test
    void testGetAllEpicsEmpty() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/epics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void testCreateEpic() throws Exception {
        Epic epic = new Epic("Epic 1", "desc", 0, Status.NEW);
        String body = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        Epic created = gson.fromJson(response.body(), Epic.class);
        assertNotEquals(0, created.getId());
    }

    @Test
    void testUpdateEpic() throws Exception {
        Epic epic = manager.createEpic(new Epic("Epic", "desc", 1, Status.NEW));
        epic.setName("Updated Epic");
        String body = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic updated = gson.fromJson(response.body(), Epic.class);
        assertEquals("Updated Epic", updated.getName());
    }

    @Test
    void testGetEpicByIdNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/epics/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void testDeleteEpicNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/epics/999"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode());
    }
}
