package main.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.http.BaseHttpHandler;
import main.manager.TaskManager;
import main.models.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String path = h.getRequestURI().getPath();
            String[] parts = path.split("/");

            if ("GET".equals(method)) {
                if (parts.length == 2) { // /tasks
                    sendText(h, gson.toJson(manager.getAllTasks()), 200);
                } else if (parts.length == 3) {
                    int id = Integer.parseInt(parts[2]);
                    Task task = manager.getTask(id);
                    if (task == null) {
                        sendNotFound(h, "Задача с id=" + id + " не найдена");
                    } else {
                        sendText(h, gson.toJson(task), 200);
                    }
                }
            } else if ("POST".equals(method)) {
                InputStream is = h.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);
                if (task.getId() == 0) {
                    manager.createTask(task);
                    sendText(h, gson.toJson(task), 201);
                } else {
                    manager.updateTask(task);
                    sendText(h, gson.toJson(task), 200);
                }
            } else if ("DELETE".equals(method) && parts.length == 3) {
                int id = Integer.parseInt(parts[2]);
                manager.deleteTask(id);
                sendNoContent(h);
            }
        } catch (Exception e) {
            sendError(h, e.getMessage());
        }
    }
}
