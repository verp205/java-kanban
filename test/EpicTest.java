import main.enums.Status;
import main.enums.TaskType;
import main.models.Epic;
import main.models.Subtask;
import main.manager.FileBackedTaskManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
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
    void cannotAddEpicToItselfAsSubtask() throws IOException {
        File tempFile = File.createTempFile("testTasks", ".txt");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        Epic epic = new Epic("Epic", "Desc", 1, Status.NEW);
        manager.createEpic(epic);

        Subtask subtask = new Subtask(
                "Epic",
                "Desc",
                epic.getId(),
                Status.NEW,
                epic.getId(),
                LocalDateTime.now(),
                Duration.ofHours(1)
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.createSubtask(subtask);
        });

        assertTrue(exception.getMessage().contains("id"), "Должна быть ошибка из-за совпадения id");
    }

}
