import main.manager.FileBackedTaskManager;
import main.models.Task;
import main.models.Epic;
import main.models.Subtask;
import main.enums.Status;
import main.enums.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
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
    void shouldCorrectlySaveAndLoadEmptyManager() {
        // Создаем новый менеджер через загрузку
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Задачи должны быть пустыми");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Эпики должны быть пустыми");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Подзадачи должны быть пустыми");
    }

    @Test
    void shouldSaveAndLoadTasksCorrectly() {
        // Создаем тестовые данные
        Task task = new Task("Task", "Desc", 1, Status.NEW, TaskType.TASK);
        Epic epic = new Epic("Epic", "Desc", 2, Status.NEW);
        Subtask subtask = new Subtask("Subtask", "Desc", 3, Status.NEW, 2);

        // Добавляем задачи (каждый вызов создает новое сохранение)
        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubtask(subtask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Сравниваем списки целиком
        assertEquals(manager.getAllTasks(), loadedManager.getAllTasks(), "Списки задач не совпадают");
        assertEquals(manager.getAllEpics(), loadedManager.getAllEpics(), "Списки эпиков не совпадают");
        assertEquals(manager.getAllSubtasks(), loadedManager.getAllSubtasks(), "Списки подзадач не совпадают");
    }

    @Test
    void shouldMaintainEpicSubtaskRelationships() {
        Epic epic = new Epic("Epic", "Desc", 1, Status.NEW);
        Subtask subtask = new Subtask("Subtask", "Desc", 2, Status.NEW, 1);

        manager.createEpic(epic);
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем, что связь сохранилась через сравнение списков подзадач эпика
        assertEquals(
                manager.getEpic(1).getSubIds(),
                loadedManager.getEpic(1).getSubIds(),
                "Связи эпик-подзадача не сохранились"
        );
    }

    @Test
    void shouldSaveAndLoadAllTasks() {
        // Создаем тестовые данные
        Task task = new Task("Task", "Description", 1, Status.NEW, TaskType.TASK);
        Epic epic = new Epic("Epic", "Description", 2, Status.NEW);
        Subtask subtask = new Subtask("Subtask", "Description", 3, Status.NEW, 2);

        // Добавляем (автоматически сохраняется)
        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubtask(subtask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем что все задачи сохранились
        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        // Проверяем связь эпик-подзадача
        assertEquals(2, loadedManager.getSubtask(3).getEpicId());
        assertTrue(loadedManager.getEpic(2).getSubIds().contains(3));
    }
}