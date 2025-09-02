import main.enums.Status;
import main.manager.FileBackedTaskManager;
import main.models.Epic;
import main.models.Subtask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            tempFile = File.createTempFile("tasks", ".csv");
            return new FileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }
    }

    @AfterEach
    void deleteTempFile() throws IOException {
        Files.deleteIfExists(tempFile.toPath());
    }

    @Test
    void loadEmptyManager_shouldReturnEmptyTaskLists() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertAll(
                () -> assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пуст"),
                () -> assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пуст"),
                () -> assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пуст")
        );
    }

    @Test
    void saveAndLoad_withTasksAndSubtasks_shouldRestoreStateCorrectly() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Epic epic = manager.createEpic(new Epic("Epic", "Description", 0, Status.NEW));
        Subtask subtask = new Subtask("Subtask", "Description", 0,
                Status.IN_PROGRESS, epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        subtask = manager.createSubtask(subtask);

        manager.saveToFile();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверки
        assertEquals(1, loadedManager.getAllEpics().size(), "Должен быть один эпик");
        assertEquals(1, loadedManager.getAllSubtasks().size(), "Должна быть одна подзадача");

        Epic loadedEpic = loadedManager.getEpic(epic.getId());
        assertNotNull(loadedEpic, "Эпик после загрузки не должен быть null");
        assertEquals(Status.IN_PROGRESS, loadedEpic.getStatus(), "Статус эпика должен быть IN_PROGRESS");

        Subtask loadedSubtask = loadedManager.getSubtask(subtask.getId());
        assertEquals(epic.getId(), loadedSubtask.getEpicId(), "Подзадача должна ссылаться на правильный эпик");
    }


    @Test
    void epicSubtaskRelationship_shouldBeMaintainedAfterLoad() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Desc", 0,
                Status.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(15)));

        manager.saveToFile();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpic(epic.getId());

        assertNotNull(loadedEpic, "Эпик должен загрузиться из файла");
        assertEquals(1, loadedEpic.getSubtaskIds().size(), "У эпика должна быть одна подзадача");
        assertTrue(loadedEpic.getSubtaskIds().contains(subtask.getId()), "ID подзадачи должен присутствовать в списке");

    }

    @Test
    void loadingFromNonExistentFile_shouldThrowException() {
        File nonExistentFile = new File("nonexistent_dir/nonexistent.csv");

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                FileBackedTaskManager.loadFromFile(nonExistentFile));

        assertTrue(exception.getMessage().contains("Ошибка загрузки") || exception.getCause() instanceof IOException,
                "Должно быть выброшено исключение о невозможности загрузки файла");
    }
}
