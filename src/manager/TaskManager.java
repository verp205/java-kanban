package manager;
import models.Task;
import models.Epic;
import models.Subtask;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    ArrayList<Task> getAllTasks();
    void deleteAllTasks();
    Task getTask(int id);
    Task createTask(Task task);
    void updateTask(Task task);
    void deleteTask(int id);

    ArrayList<Epic> getAllEpics();
    void deleteAllEpics();
    Epic getEpic(int id);
    Epic createEpic(Epic epic);
    void updateEpic(Epic epic);
    void deleteEpic(int id);

    ArrayList<Subtask> getAllSubtasks();
    void deleteAllSubtasks();
    Subtask getSubtask(int id);
    Subtask createSubtask(Subtask subtask);
    void updateSubtask(Subtask subtask);
    void deleteSubtask(int id);

    List<Task> getHistory();
}
