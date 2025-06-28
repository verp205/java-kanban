import main.enums.Status;
import main.models.Epic;
import main.models.Subtask;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Epic 1", "Description 1", 1, Status.NEW);
        Epic epic2 = new Epic("Epic 2", "Description 2", 1, Status.DONE);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны");
    }

    @Test
    void cannotAddEpicToItselfAsSubtask() {
        Epic epic = new Epic("Epic", "Description", 1, Status.NEW);
        Subtask subtask = new Subtask("Subtask", "Description", 2, Status.NEW, 2); // epicId=2, а у эпика ID=1

        assertNotEquals(epic.getId(), subtask.getEpicId(),
                "Эпик не должен быть подзадачей самого себя");
    }
}