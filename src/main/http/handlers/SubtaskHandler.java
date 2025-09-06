package main.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.http.BaseHttpHandler;
import main.manager.TaskManager;
import main.models.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    public SubtaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String path = h.getRequestURI().getPath();
            String[] parts = path.split("/");

            if ("GET".equals(method)) {
                if (parts.length == 2) { // /subtasks
                    sendText(h, gson.toJson(manager.getAllSubtasks()), 200);
                } else if (parts.length == 3) {
                    int id = Integer.parseInt(parts[2]);
                    Subtask subtask = manager.getSubtask(id);
                    if (subtask == null) {
                        sendNotFound(h, "Подзадача с id=" + id + " не найдена");
                    } else {
                        sendText(h, gson.toJson(subtask), 200);
                    }
                }
            } else if ("POST".equals(method)) {
                InputStream is = h.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Subtask subtask = gson.fromJson(body, Subtask.class);
                if (subtask.getId() == 0) {
                    manager.createSubtask(subtask);
                    sendText(h, gson.toJson(subtask), 201);
                } else {
                    manager.updateSubtask(subtask);
                    sendText(h, gson.toJson(subtask), 200);
                }
            } else if ("DELETE".equals(method) && parts.length == 3) {
                int id = Integer.parseInt(parts[2]);
                manager.deleteSubtask(id);
                sendNoContent(h);
            }
        } catch (Exception e) {
            sendError(h, e.getMessage());
        }
    }
}
