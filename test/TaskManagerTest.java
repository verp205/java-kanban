import main.enums.Status;
import main.manager.TaskManager;
import main.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    void shouldCreateAndGetTask() {
        Task task = new Task("Task", "Description", 0, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));

        Task created = manager.createTask(task);
        Task retrieved = manager.getTask(created.getId());

        assertNotNull(retrieved);
        assertEquals(created, retrieved);
    }

    @Test
    void shouldCreateAndGetEpic() {
        Epic epic = new Epic("Epic", "Description", 0, Status.NEW);
        Epic created = manager.createEpic(epic);
        Epic retrieved = manager.getEpic(created.getId());

        assertNotNull(retrieved);
        assertEquals(created, retrieved);
    }

    @Test
    void shouldCreateAndGetSubtask() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask subtask = new Subtask("Subtask", "Desc", 0, Status.NEW,
                epic.getId(), LocalDateTime.now(), Duration.ofMinutes(30));

        Subtask created = manager.createSubtask(subtask);
        Subtask retrieved = manager.getSubtask(created.getId());

        assertNotNull(retrieved);
        assertEquals(created, retrieved);
        assertEquals(epic.getId(), retrieved.getEpicId());
    }

    @Test
    void shouldUpdateTask() {
        Task task = manager.createTask(new Task("Task", "Desc", 0, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));

        Task updated = new Task("Updated", "New Desc", task.getId(), Status.IN_PROGRESS,
                task.getStartTime(), task.getDuration());

        manager.updateTask(updated);
        assertEquals(updated, manager.getTask(task.getId()));
    }

    @Test
    void shouldUpdateEpicTimeWhenSubtaskRemoved() {
        // 1. Создаем эпик
        Epic epic = manager.createEpic(new Epic("Test Epic", "Description", 0, Status.NEW));
        int epicId = epic.getId();

        // 2. Добавляем подзадачи
        LocalDateTime baseTime = LocalDateTime.of(2023, 1, 1, 10, 0);

        Subtask sub1 = manager.createSubtask(
                new Subtask("Sub1", "Desc", 0, Status.NEW,
                        epicId, baseTime, Duration.ofMinutes(30))
        );

        Subtask sub2 = manager.createSubtask(
                new Subtask("Sub2", "Desc", 0, Status.NEW,
                        epicId, baseTime.plusHours(1), Duration.ofMinutes(30))
        );

        // 3. Проверяем начальное время эпика
        assertEquals(baseTime, manager.getEpic(epicId).getStartTime());
        assertEquals(baseTime.plusHours(1).plusMinutes(30), manager.getEpic(epicId).getEndTime());

        // 4. Удаляем подзадачу
        manager.deleteSubtask(sub2.getId());

        // 5. Проверяем обновленное время
        assertEquals(baseTime, manager.getEpic(epicId).getStartTime());
        assertEquals(baseTime.plusMinutes(30), manager.getEpic(epicId).getEndTime());

        // 6. Удаляем последнюю подзадачу
        manager.deleteSubtask(sub1.getId());

        // 7. Проверяем, что время сброшено
        assertNull(manager.getEpic(epicId).getStartTime());
        assertNull(manager.getEpic(epicId).getEndTime());
    }

    // Вспомогательные методы для улучшения читаемости
    private Subtask createSubtaskAtTime(Epic epic, String name, Status status, LocalDateTime startTime) {
        return manager.createSubtask(new Subtask(
                name, "Description", 0, status,
                epic.getId(),
                startTime,
                Duration.ofMinutes(30))
        );
    }

    private void updateSubtaskStatus(Subtask subtask, Status newStatus) {
        Subtask updated = new Subtask(
                subtask.getName(), subtask.getDescription(),
                subtask.getId(), newStatus,
                subtask.getEpicId(),
                subtask.getStartTime(),
                subtask.getDuration()
        );
        manager.updateSubtask(updated);
    }

    @Test
    void shouldNotAllowTimeOverlaps() {
        Task task1 = manager.createTask(new Task("Task1", "Desc", 0, Status.NEW,
                LocalDateTime.of(2025, 1, 1, 10, 0), Duration.ofHours(1)));

        assertThrows(IllegalArgumentException.class, () -> {
            Task task2 = new Task("Task2", "Desc", 0, Status.NEW,
                    LocalDateTime.of(2025, 1, 1, 10, 30), Duration.ofHours(1));
            manager.createTask(task2);
        });
    }

    @Test
    void shouldDeleteTaskById() {
        Task task = manager.createTask(new Task("Task", "Desc", 0, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.deleteTask(task.getId());
        assertNull(manager.getTask(task.getId()));
    }

    @Test
    void shouldDeleteAllTasks() {
        manager.createTask(new Task("Task1", "Desc", 0, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.createTask(new Task("Task2", "Desc", 0, Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)));

        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void shouldDeleteEpicAndSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask subtask = manager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW,
                epic.getId(), LocalDateTime.now(), Duration.ofMinutes(30)));

        manager.deleteEpic(epic.getId());

        assertNull(manager.getEpic(epic.getId()));
        assertNull(manager.getSubtask(subtask.getId()));
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldDeleteAllEpicsAndSubtasks() {
        Epic epic1 = manager.createEpic(new Epic("Epic1", "Desc", 0, Status.NEW));
        Epic epic2 = manager.createEpic(new Epic("Epic2", "Desc", 0, Status.NEW));
        manager.createSubtask(new Subtask("Sub1", "Desc", 0, Status.NEW,
                epic1.getId(), LocalDateTime.now(), Duration.ofMinutes(30)));

        manager.deleteAllEpics();

        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldDeleteAllSubtasksOnly() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask subtask = manager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW,
                epic.getId(), LocalDateTime.now(), Duration.ofMinutes(30)));

        manager.deleteAllSubtasks();

        assertTrue(manager.getAllSubtasks().isEmpty());
        assertTrue(manager.getEpic(epic.getId()).getSubtaskIds().isEmpty());
    }

    @Test
    void shouldReturnAllTasks() {
        Task task1 = manager.createTask(new Task("Task1", "Desc", 0, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        Task task2 = manager.createTask(new Task("Task2", "Desc", 0, Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)));

        assertEquals(2, manager.getAllTasks().size());
    }

    @Test
    void shouldReturnAllEpicsAndSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        manager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW,
                epic.getId(), LocalDateTime.now(), Duration.ofMinutes(30)));

        assertEquals(1, manager.getAllEpics().size());
        assertEquals(1, manager.getAllSubtasks().size());
    }

    @Test
    void shouldReturnPrioritizedTasksInOrder() {
        Task task1 = manager.createTask(new Task("Task1", "Desc", 0, Status.NEW,
                LocalDateTime.of(2025, 1, 1, 10, 0), Duration.ofMinutes(30)));
        Task task2 = manager.createTask(new Task("Task2", "Desc", 0, Status.NEW,
                LocalDateTime.of(2025, 1, 1, 9, 0), Duration.ofMinutes(30)));

        var prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals(task2.getId(), prioritized.get(0).getId());
        assertEquals(task1.getId(), prioritized.get(1).getId());
    }

    @Test
    void shouldTrackHistoryOfAccessedTasks() {
        Task task = manager.createTask(new Task("Task", "Desc", 0, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));

        manager.getTask(task.getId());
        var history = manager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
    }

}