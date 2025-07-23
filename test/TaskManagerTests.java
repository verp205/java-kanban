import main.enums.Priority;
import main.enums.Status;
import main.enums.TaskType;
import main.models.*;
import main.manager.FileBackedTaskManager;
import main.manager.InMemoryTaskManager;
import main.manager.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTests {
    private TaskManager manager;

    @BeforeEach
    public void setup() {
        manager = new InMemoryTaskManager();
    }

    @Test
    public void testPriorityIsSavedAndLoaded() throws IOException {
        File file = new File("tasks.csv");
        FileBackedTaskManager fileManager = new FileBackedTaskManager(file);

        Task task = new Task("Task A", "desc", 0, Status.NEW, TaskType.TASK,
                LocalDateTime.now(), Duration.ofHours(1));

        task.setPriority(Priority.HIGH);
        fileManager.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        List<Task> loadedTasks = loaded.getAllTasks();

        assertEquals(1, loadedTasks.size());
        assertEquals(Priority.HIGH, loadedTasks.get(0).getPriority());
    }

    @Test
    public void testNoTimeOverlapAllowed() {
        Task task1 = new Task("Task 1", "desc", 0, Status.NEW, TaskType.TASK,
                LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofHours(1));
        Task task2 = new Task("Task 2", "desc", 0, Status.NEW, TaskType.TASK,
                LocalDateTime.of(2025, 1, 1, 12, 30), Duration.ofHours(1));  // Пересекается

        manager.createTask(task1);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> manager.createTask(task2));
        assertTrue(ex.getMessage().contains("Пересечение"));
    }

    @Test
    public void testEpicTimeCalculation() {
        Epic epic = new Epic("Epic 1", "desc", 0, Status.NEW);
        epic = manager.createEpic(epic); // id присвоится

        Subtask sub1 = new Subtask("Sub1", "desc", 0, Status.NEW, epic.getId(),
                LocalDateTime.of(2025, 1, 1, 10, 0), Duration.ofHours(1));
        Subtask sub2 = new Subtask("Sub2", "desc", 0, Status.NEW, epic.getId(),
                LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofHours(2));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        Epic updated = manager.getEpic(epic.getId());  // правильный метод
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), updated.getStartTime());
        assertEquals(LocalDateTime.of(2025, 1, 1, 14, 0), updated.getEndTime());
        assertEquals(Duration.ofHours(3), updated.getDuration());
    }

    @Test
    public void testTaskToCsvAndFromCsv() throws IOException {
        File file = new File("tasks_test.csv");
        FileBackedTaskManager mgr = new FileBackedTaskManager(file);

        Task task = new Task("Task 2", "desc", 0, Status.NEW, TaskType.TASK,
                LocalDateTime.of(2025, 7, 1, 10, 0), Duration.ofHours(1));
        task.setPriority(Priority.MEDIUM);
        mgr.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loaded.getAllTasks().get(0);

        Assertions.assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());
        assertEquals(task.getDuration(), loadedTask.getDuration());
        assertEquals(task.getEndTime(), loadedTask.getEndTime());
        assertEquals(task.getPriority(), loadedTask.getPriority());
    }
}
