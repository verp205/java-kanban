package tests;

import main.enums.Status;
import main.manager.InMemoryTaskManager;
import main.models.Epic;
import main.models.Subtask;
import main.models.Task;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private final InMemoryTaskManager manager = new InMemoryTaskManager();

    @Test
    void createTask_shouldAddTask() {
        Task task = new Task("Task", "Description", 1, Status.NEW);
        manager.createTask(task);
        assertEquals(task, manager.getTask(1), "Задача должна быть добавлена");
    }

    @Test
    void updateTask_shouldUpdateTask() {
        Task task = new Task("Task", "Description", 1, Status.NEW);
        manager.createTask(task);
        Task updated = new Task("Updated", "New Description", 1, Status.DONE);
        manager.updateTask(updated);
        Task fromManager = manager.getTask(1);
        assertEquals("Updated", fromManager.getName(), "Имя задачи должно обновиться");
        assertEquals("New Description", fromManager.getDesc(), "Описание задачи должно обновиться");
        assertEquals(Status.DONE, fromManager.getStatus(), "Статус задачи должен обновиться");
    }

    @Test
    void deleteTask_shouldRemoveTask() {
        Task task = new Task("Task", "Description", 1, Status.NEW);
        manager.createTask(task);
        manager.deleteTask(1);
        assertNull(manager.getTask(1), "Задача должна быть удалена");
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        Task task1 = new Task("Task 1", "Description", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Description", 2, Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        assertEquals(2, manager.getAllTasks().size(), "Должны вернуться все задачи");
    }

    @Test
    void deleteAllTasks_shouldClearTasks() {
        manager.createTask(new Task("Task", "Description", 1, Status.NEW));
        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty(), "Все задачи должны быть удалены");
    }

    // Аналогичные тесты для Epic и Subtask
    @Test
    void createEpic_shouldAddEpic() {
        Epic epic = new Epic("Epic", "Description", 1, Status.NEW);
        manager.createEpic(epic);
        assertEquals(epic, manager.getEpic(1), "Эпик должен быть добавлен");
    }

    @Test
    void createSubtask_shouldAddSubtaskAndUpdateEpic() {
        Epic epic = new Epic("Epic", "Description", 1, Status.NEW);
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Description", 2, Status.NEW, 1);
        manager.createSubtask(subtask);
        assertEquals(subtask, manager.getSubtask(2), "Подзадача должна быть добавлена");
        assertTrue(manager.getEpic(1).getSubIds().contains(2), "Эпик должен содержать подзадачу");
    }

    @Test
    void getHistory_shouldReturnHistory() {
        Task task = new Task("Task", "Description", 1, Status.NEW);
        manager.createTask(task);
        manager.getTask(1);
        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История должна содержать просмотренную задачу");
        assertEquals(task, history.get(0), "Задача в истории должна совпадать с просмотренной");
    }
}