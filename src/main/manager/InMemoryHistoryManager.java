package main.manager;

import main.models.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedList<Task> history = new LinkedList<>();
    private static final int MAX_HISTORY_SIZE = 10;

    @Override
    public void add(Task task) {
        if (task == null) return;

        history.addFirst(task);

        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeLast();
        }
    }

    @Override
    public List<Task> getHistory() {
        return Collections.unmodifiableList(new LinkedList<>(history));
    }
}