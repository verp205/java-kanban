package main.manager;
import main.models.Task;
import main.models.Epic;
import main.models.Subtask;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    ArrayList<Task> getAllTasks();

    Task getTask(int id);

    void deleteAllTasks();

    Task createTask(Task task);

    void updateTask(Task task);

    void deleteTask(int id);

    ArrayList<Epic> getAllEpics();

    Epic getEpic(int id);

    void deleteAllEpics();

    Epic createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpic(int id);

    ArrayList<Subtask> getAllSubtasks();

    Subtask getSubtask(int id);

    void deleteAllSubtasks();

    Subtask createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(int id);

    List<Task> getPrioritizedTasks();

    List<Task> getHistory();
}
