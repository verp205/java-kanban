package main.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.http.BaseHttpHandler;
import main.manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    public HistoryHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            if ("GET".equals(h.getRequestMethod())) {
                sendText(h, gson.toJson(manager.getHistory()), 200);
            } else {
                sendMethodNotAllowed(h, h.getRequestMethod());
            }
        } catch (Exception e) {
            sendError(h, e.getMessage());
        }
    }
}
