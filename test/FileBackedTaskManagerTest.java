import main.manager.FileBackedTaskManager;
import main.models.Task;
import main.models.Epic;
import main.models.Subtask;
import main.enums.Status;
import main.enums.TaskType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile.toPath());
    }

    @Test
    void shouldCorrectlySaveAndLoadEmptyManager() throws IOException {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Задачи должны быть пустыми");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Эпики должны быть пустыми");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Подзадачи должны быть пустыми");
    }

    @Test
    void shouldSaveAndLoadTasksCorrectly() throws IOException {
        Task task = new Task(
                "Task",
                "desc",
                0,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.now(),
                Duration.ofHours(1)
        );

        Epic epic = new Epic("Epic", "Desc", 2, Status.NEW);

        Subtask subtask = new Subtask(
                "Subtask 1",
                "desc",
                3,
                Status.NEW,
                epic.getId(),
                LocalDateTime.of(2025, 1, 1, 10, 0),
                Duration.ofMinutes(30)
        );

        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(manager.getAllTasks(), loadedManager.getAllTasks(), "Списки задач не совпадают");
        assertEquals(manager.getAllEpics(), loadedManager.getAllEpics(), "Списки эпиков не совпадают");
        assertEquals(manager.getAllSubtasks(), loadedManager.getAllSubtasks(), "Списки подзадач не совпадают");
    }

    @Test
    void shouldMaintainEpicSubtaskRelationships() throws IOException {
        Epic epic = new Epic("Epic", "Desc", 1, Status.NEW);

        Subtask subtask = new Subtask(
                "Subtask",
                "desc",
                3,
                Status.NEW,
                epic.getId(),
                LocalDateTime.of(2025, 1, 1, 10, 0),
                Duration.ofMinutes(30)
        );

        manager.createEpic(epic);
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(
                manager.getEpic(1).getSubIds(),
                loadedManager.getEpic(1).getSubIds(),
                "Связи эпик-подзадача не сохранились"
        );
    }

    @Test
    void shouldSaveAndLoadAllTasks() throws IOException {
        Task task = new Task(
                "Task",
                "desc",
                0,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.now(),
                Duration.ofHours(1)
        );

        Epic epic = new Epic("Epic", "Description", 2, Status.NEW);

        Subtask subtask = new Subtask(
                "Subtask",
                "desc",
                3,
                Status.NEW,
                epic.getId(),
                LocalDateTime.of(2025, 1, 1, 10, 0),
                Duration.ofMinutes(30)
        );

        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        assertEquals(2, loadedManager.getSubtask(3).getEpicId());
        assertTrue(loadedManager.getEpic(2).getSubIds().contains(3));
    }

    @Test
    public void shouldCalculateEpicStatus_AllNew() {
        Epic epic = new Epic("Epic", "Desc", 1, Status.NEW);
        Subtask s1 = new Subtask("Sub1", "Desc", 2, Status.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(30));
        Subtask s2 = new Subtask("Sub2", "Desc", 3, Status.NEW, epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));

        manager.createEpic(epic);
        manager.createSubtask(s1);
        manager.createSubtask(s2);

        assertEquals(Status.NEW, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    public void shouldCalculateEpicStatus_AllDone() {
        Epic epic = new Epic("Epic", "Desc", 1, Status.NEW);
        Subtask s1 = new Subtask("Sub1", "Desc", 2, Status.DONE, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(30));
        Subtask s2 = new Subtask("Sub2", "Desc", 3, Status.DONE, epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));

        manager.createEpic(epic);
        manager.createSubtask(s1);
        manager.createSubtask(s2);

        assertEquals(Status.DONE, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    public void shouldCalculateEpicStatus_NewAndDone() {
        Epic epic = new Epic("Epic", "Desc", 1, Status.NEW);
        Subtask s1 = new Subtask("Sub1", "Desc", 2, Status.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(30));
        Subtask s2 = new Subtask("Sub2", "Desc", 3, Status.DONE, epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));

        manager.createEpic(epic);
        manager.createSubtask(s1);
        manager.createSubtask(s2);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    public void shouldCalculateEpicStatus_WithInProgress() {
        Epic epic = new Epic("Epic", "Desc", 1, Status.NEW);
        Subtask s1 = new Subtask("Sub1", "Desc", 2, Status.IN_PROGRESS, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(30));
        Subtask s2 = new Subtask("Sub2", "Desc", 3, Status.NEW, epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));

        manager.createEpic(epic);
        manager.createSubtask(s1);
        manager.createSubtask(s2);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    public void shouldNotAddTaskIfTimeIntersect() {
        Task task1 = new Task(
                "Task1",
                "Desc",
                1,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.of(2025, 7, 24, 10, 0),
                Duration.ofMinutes(60)
        );
        Task task2 = new Task(
                "Task2",
                "Desc",
                2,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.of(2025, 7, 24, 10, 30),
                Duration.ofMinutes(30) // пересекается
        );

        manager.createTask(task1);
        assertThrows(IllegalArgumentException.class, () -> manager.createTask(task2));
    }

    @Test
    public void shouldNotUpdateTaskIfTimeIntersect() {
        Task task1 = new Task(
                "Task1",
                "Desc",
                1,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.of(2025, 7, 24, 10, 0),
                Duration.ofMinutes(60)
        );
        Task task2 = new Task(
                "Task2",
                "Desc",
                2,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.of(2025, 7, 24, 12, 0),
                Duration.ofMinutes(30)
        );

        manager.createTask(task1);
        manager.createTask(task2);

        // Обновляем task2 так, чтобы пересекалось время с task1
        Task updatedTask2 = new Task(
                task2.getName(),
                task2.getDescription(),
                task2.getId(),
                task2.getStatus(),
                task2.getType(),
                LocalDateTime.of(2025, 7, 24, 10, 30),
                Duration.ofMinutes(30)
        );

        assertThrows(IllegalArgumentException.class, () -> manager.updateTask(updatedTask2));
    }

    @Test
    public void shouldExcludeTasksWithoutStartTimeFromPrioritized() {
        Task task1 = new Task(
                "Task1",
                "Desc",
                1,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.of(2025, 7, 24, 9, 0),
                Duration.ofMinutes(30)
        );
        Task task2 = new Task(
                "Task2",
                "Desc",
                2,
                Status.NEW,
                TaskType.TASK,
                null,
                null // без времени
        );

        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertTrue(prioritized.contains(task1));
        assertFalse(prioritized.contains(task2));
    }

    @Test
    public void shouldThrowExceptionWhenFileNotFound() {
        FileBackedTaskManager fileManager = new FileBackedTaskManager(new File("nonexistent_dir/nonexistent_file.csv"));
        assertThrows(IOException.class, () -> fileManager.loadFromFile(fileManager.file));
    }

    @Test
    public void shouldNotThrowExceptionWhenSavingAndLoading() {
        assertDoesNotThrow(() -> {
            manager.save();
            FileBackedTaskManager loadedManager = manager.loadFromFile(manager.file);
            assertNotNull(loadedManager);
        });
    }

}
