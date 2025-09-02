package main.http.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.http.BaseHttpHandler;
import main.manager.TaskManager;
import main.models.Epic;
import main.models.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    private Gson createGson() {
        GsonBuilder builder = new GsonBuilder();

        // Адаптер для LocalDateTime
        builder.registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        builder.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Адаптер для Duration
        builder.registerTypeAdapter(Duration.class, (JsonSerializer<Duration>) (src, typeOfSrc, context) ->
                new JsonPrimitive(src.toMinutes()));
        builder.registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>) (json, typeOfT, context) ->
                Duration.ofMinutes(json.getAsLong()));

        return builder.create();
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String path = h.getRequestURI().getPath();
            String[] parts = path.split("/");

            if ("GET".equals(method)) {
                if (parts.length == 2) { // /epics
                    sendText(h, gson.toJson(manager.getAllEpics()), 200);
                } else if (parts.length == 3) { // /epics/{id}
                    int id = Integer.parseInt(parts[2]);
                    Epic epic = manager.getEpic(id);
                    if (epic == null) {
                        sendNotFound(h, "Эпик с id=" + id + " не найден");
                    } else {
                        sendText(h, gson.toJson(epic), 200);
                    }
                } else if (parts.length == 4 && "subtasks".equals(parts[3])) { // /epics/{id}/subtasks
                    int id = Integer.parseInt(parts[2]);
                    List<Subtask> subtasks = manager.getAllSubtasks().stream()
                            .filter(st -> st.getEpicId() == id)
                            .collect(Collectors.toList());
                    sendText(h, gson.toJson(subtasks), 200);
                }
            } else if ("POST".equals(method)) {
                InputStream is = h.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Epic epic = gson.fromJson(body, Epic.class);
                manager.createEpic(epic);
                sendText(h, gson.toJson(epic), 201);
            } else if ("DELETE".equals(method) && parts.length == 3) {
                int id = Integer.parseInt(parts[2]);
                manager.deleteEpic(id);
                sendText(h, "{\"result\":\"deleted\"}", 200);
            }
        } catch (Exception e) {
            sendError(h, e.getMessage());
        }
    }
}
