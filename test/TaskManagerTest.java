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
    void shouldCalculateEpicStatus() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));

        Subtask sub1 = manager.createSubtask(new Subtask("Sub1", "Desc", 0, Status.NEW,
                epic.getId(), LocalDateTime.now(), Duration.ofMinutes(30)));
        assertEquals(Status.NEW, manager.getEpic(epic.getId()).getStatus(),
                "Эпик должен быть NEW, когда все подзадачи NEW");

        Subtask sub2 = manager.createSubtask(new Subtask("Sub2", "Desc", 0, Status.DONE,
                epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)));
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus(),
                "Эпик должен быть IN_PROGRESS, когда есть подзадачи NEW и DONE");

        manager.updateSubtask(new Subtask(sub1.getName(), sub1.getDescription(),
                sub1.getId(), Status.DONE, sub1.getEpicId(),
                sub1.getStartTime(), sub1.getDuration()));
        assertEquals(Status.DONE, manager.getEpic(epic.getId()).getStatus(),
                "Эпик должен быть DONE, когда все подзадачи DONE");

        Subtask sub3 = manager.createSubtask(new Subtask("Sub3", "Desc", 0, Status.IN_PROGRESS,
                epic.getId(), LocalDateTime.now().plusHours(2), Duration.ofMinutes(30)));
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus(),
                "Эпик должен быть IN_PROGRESS, когда есть подзадача IN_PROGRESS");
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
}