import main.enums.Status;
import main.manager.InMemoryTaskManager;
import main.models.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void shouldPrioritizeTasksByStartTime() {
        Task task1 = manager.createTask(new Task("Task1", "Desc", 0, Status.NEW,
                LocalDateTime.of(2025, 1, 2, 10, 0), Duration.ofHours(1)));

        Task task2 = manager.createTask(new Task("Task2", "Desc", 0, Status.NEW,
                LocalDateTime.of(2025, 1, 1, 9, 0), Duration.ofHours(1)));

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(2, prioritized.size());
        assertEquals(task2.getId(), prioritized.get(0).getId());
        assertEquals(task1.getId(), prioritized.get(1).getId());
    }

    @Test
    void shouldHandleTasksWithoutTime() {
        Task task1 = manager.createTask(new Task("Task1", "Desc", 0, Status.NEW,
                null, null));

        Task task2 = manager.createTask(new Task("Task2", "Desc", 0, Status.NEW,
                LocalDateTime.of(2025, 1, 1, 9, 0), Duration.ofHours(1)));

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(2, prioritized.size());
        assertEquals(task2.getId(), prioritized.get(0).getId()); // Задача со временем должна быть первой
    }

    @Test
    void shouldUpdateEpicTimeWhenSubtasksChange() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));

        Subtask subtask1 = manager.createSubtask(new Subtask("Sub1", "Desc", 0, Status.NEW,
                epic.getId(), LocalDateTime.of(2025, 1, 1, 10, 0), Duration.ofHours(1)));

        Subtask subtask2 = manager.createSubtask(new Subtask("Sub2", "Desc", 0, Status.NEW,
                epic.getId(), LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofHours(1)));

        Epic updatedEpic = manager.getEpic(epic.getId());

        assertEquals(subtask1.getStartTime(), updatedEpic.getStartTime());
        assertEquals(subtask2.getEndTime(), updatedEpic.getEndTime());

        // Удаляем подзадачу и проверяем обновление времени
        manager.deleteSubtask(subtask2.getId());
        updatedEpic = manager.getEpic(epic.getId());
        assertEquals(subtask1.getStartTime(), updatedEpic.getStartTime());
        assertEquals(subtask1.getEndTime(), updatedEpic.getEndTime());
    }

    @Test
    void shouldAddTasksToHistory() {
        Task task = manager.createTask(new Task("Task", "Desc", 0, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));

        Epic epic = manager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));

        Subtask subtask = manager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW,
                epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)));

        // Получаем задачи, чтобы добавить в историю
        manager.getTask(task.getId());
        manager.getEpic(epic.getId());
        manager.getSubtask(subtask.getId());

        assertEquals(3, manager.getHistory().size());
    }

    @Test
    void shouldNotDuplicateTasksInHistory() {
        Task task = manager.createTask(new Task("Task", "Desc", 0, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));

        manager.getTask(task.getId());
        manager.getTask(task.getId());
        manager.getTask(task.getId());

        assertEquals(1, manager.getHistory().size());
    }

    @Test
    void shouldRemoveTaskFromHistoryWhenDeleted() {
        Task task = manager.createTask(new Task("Task", "Desc", 0, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));

        manager.getTask(task.getId());
        manager.deleteTask(task.getId());

        assertTrue(manager.getHistory().isEmpty());
    }
}