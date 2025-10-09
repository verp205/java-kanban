package main.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import main.manager.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler {
    protected final TaskManager manager;
    protected final Gson gson;

    protected BaseHttpHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    protected void sendText(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1); // тело не требуется
        exchange.close();
    }

    protected void sendError(HttpExchange exchange, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(500, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendMethodNotAllowed(HttpExchange exchange, String method) throws IOException {
        String message = "Метод " + method + " не поддерживается";
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(405, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
