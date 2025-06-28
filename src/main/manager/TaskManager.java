package main.manager;
import main.models.Task;
import main.models.Epic;
import main.models.Subtask;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    ArrayList<Task> getAllTasks();

    Task getTask(int id);

    Task createTask(Task task);

    void deleteTask(int id);

    ArrayList<Epic> getAllEpics();

    Epic getEpic(int id);

    Epic createEpic(Epic epic);

    void deleteEpic(int id);

    ArrayList<Subtask> getAllSubtasks();

    Subtask getSubtask(int id);

    Subtask createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(int id);

    List<Task> getHistory();
}
