import main.manager.FileBackedTaskManager;
import main.models.Task;
import main.models.Epic;
import main.models.Subtask;
import main.enums.Status;
import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(tempFile.toPath());
    }

    @Test
    void testSaveAndLoadEmptyManager() throws Exception {
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем, что загруженный менеджер пуст
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testSaveAndLoadTasks() throws Exception {
        Task task = new Task("Test Task", "Description", 1, Status.NEW);
        Epic epic = new Epic("Test Epic", "Description", 2, Status.NEW);
        Subtask subtask = new Subtask("Test Subtask", "Description", 3, Status.NEW, 2);

        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubtask(subtask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем задачи
        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        // Проверяем содержимое задач
        Task loadedTask = loadedManager.getTask(1);
        assertEquals("Test Task", loadedTask.getName());

        // Проверяем связь подзадачи с эпиком
        Subtask loadedSubtask = loadedManager.getSubtask(3);
        assertEquals(2, loadedSubtask.getEpicId());
        assertTrue(loadedManager.getEpic(2).getSubIds().contains(3));
    }

    @Test
    void testTaskFieldsAfterLoading() throws Exception {
        Task task = new Task("Task", "Desc", 1, Status.IN_PROGRESS);
        manager.createTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTask(1);

        assertEquals("Task", loadedTask.getName());
        assertEquals("Desc", loadedTask.getDesc());
        assertEquals(Status.IN_PROGRESS, loadedTask.getStatus());
    }

    @Test
    void testEpicStatusCalculationAfterLoading() throws Exception {
        Epic epic = new Epic("Epic", "Desc", 1, Status.NEW);
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Sub1", "Desc", 2, Status.DONE, 1);
        Subtask subtask2 = new Subtask("Sub2", "Desc", 3, Status.IN_PROGRESS, 1);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(Status.IN_PROGRESS, loadedManager.getEpic(1).getStatus());
    }
}