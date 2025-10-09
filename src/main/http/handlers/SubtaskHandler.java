package main.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.manager.TaskManager;
import main.models.Subtask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public SubtaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        switch (method) {
            case "GET" -> handleGet(exchange);
            case "POST" -> handlePost(exchange);
            case "PUT" -> handlePut(exchange);
            case "DELETE" -> handleDelete(exchange);
            default -> exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath(); // /subtasks or /subtasks/{id}
        String[] parts = path.split("/");

        if (parts.length == 2) {
            // Получаем все сабтаски
            List<Subtask> subtasks = manager.getAllSubtasks();
            sendResponse(exchange, 200, gson.toJson(subtasks));
        } else if (parts.length == 3) {
            // Получаем сабтаску по ID
            int id;
            try {
                id = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, -1); // неверный ID
                return;
            }
            Subtask subtask = manager.getSubtask(id);
            if (subtask == null) {
                exchange.sendResponseHeaders(404, -1); // сабтаска не найдена
            } else {
                sendResponse(exchange, 200, gson.toJson(subtask));
            }
        } else {
            exchange.sendResponseHeaders(400, -1); // некорректный путь
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            Subtask subtask = readRequestBody(exchange);
            Subtask created = manager.createSubtask(subtask);
            sendResponse(exchange, 201, gson.toJson(created));
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson("Internal server error: " + e.getMessage()));
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        try {
            Subtask subtask = readRequestBody(exchange);
            if (manager.getSubtask(subtask.getId()) != null) {
                manager.updateSubtask(subtask);
                sendResponse(exchange, 200, gson.toJson(subtask));
            } else {
                exchange.sendResponseHeaders(404, -1); // сабтаска не найдена
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson("Internal server error: " + e.getMessage()));
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if (parts.length == 2) {
            // Удаляем все сабтаски
            manager.deleteAllSubtasks();
            exchange.sendResponseHeaders(204, -1);
        } else if (parts.length == 3) {
            // Удаляем сабтаску по ID
            int id;
            try {
                id = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, -1); // неверный ID
                return;
            }
            manager.deleteSubtask(id); // <- удаляем без проверки
            exchange.sendResponseHeaders(204, -1); // всегда 204
        } else {
            exchange.sendResponseHeaders(400, -1); // некорректный путь
        }
    }

    private Subtask readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return gson.fromJson(sb.toString(), Subtask.class);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
