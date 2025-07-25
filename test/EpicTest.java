import main.enums.Status;
import main.models.Epic;
import main.models.Subtask;
import main.manager.FileBackedTaskManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Epic 1", "Description 1", 1, Status.NEW);
        Epic epic2 = new Epic("Epic 2", "Description 2", 1, Status.DONE);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны");
    }

    @Test
    void epicCannotBeItsOwnSubtask() throws IOException {
        // Создаем временный файл для менеджера задач
        Path tempFile = Files.createTempFile("test", ".csv");
        tempFile.toFile().deleteOnExit();

        // Создаем менеджер и эпик
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile.toFile());
        Epic epic = new Epic("Test Epic", "Description", 1, Status.NEW);
        manager.createEpic(epic);

        // Пытаемся создать подзадачу с ID эпика
        Subtask invalidSubtask = new Subtask(
                "Invalid Subtask",
                "Should not be allowed",
                epic.getId(), // Используем ID эпика!
                Status.NEW,
                epic.getId(),
                LocalDateTime.now(),
                Duration.ofHours(1)
        );

        // Проверяем, что попытка добавления вызывает исключение
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> manager.createSubtask(invalidSubtask),
                "Должно быть выброшено исключение при попытке сделать эпик подзадачей самого себя"
        );

        // Проверяем сообщение об ошибке
        assertTrue(
                exception.getMessage().contains("не может быть подзадачей самого себя") ||
                        exception.getMessage().contains("cannot be subtask of itself"),
                "Сообщение об ошибке должно указывать на проблему"
        );

        // Дополнительно проверяем, что подзадача не была добавлена
        assertTrue(
                manager.getSubtasks().stream().noneMatch(s -> s.getId() == epic.getId()),
                "Подзадача с ID эпика не должна быть в списке"
        );
    }
}
