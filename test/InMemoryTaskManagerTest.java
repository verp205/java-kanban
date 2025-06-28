import main.enums.Status;
import main.manager.InMemoryTaskManager;
import main.manager.TaskManager;
import main.models.Epic;
import main.models.Subtask;
import main.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void createSubtask_shouldLinkWithEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask subtask = taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));

        assertTrue(epic.getSubIds().contains(subtask.getId()));
        assertEquals(epic.getId(), subtask.getEpicId());
    }

    @Test
    void deleteSubtask_shouldRemoveFromEpicAndHistory() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask subtask = taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));
        taskManager.getSubtask(subtask.getId());
        taskManager.deleteSubtask(subtask.getId());

        assertNull(taskManager.getSubtask(subtask.getId()));
        assertFalse(epic.getSubIds().contains(subtask.getId()));
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void deleteEpic_shouldRemoveSubtasksAndHistory() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask subtask = taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask.getId());

        taskManager.deleteEpic(epic.getId());

        assertNull(taskManager.getEpic(epic.getId()));
        assertNull(taskManager.getSubtask(subtask.getId()));
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void updateSubtaskStatus_shouldUpdateEpicStatus() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask subtask = taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));

        Subtask updated = new Subtask(subtask.getName(), subtask.getDesc(), subtask.getId(), Status.DONE, epic.getId());
        taskManager.updateSubtask(updated);

        assertEquals(Status.DONE, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void getHistory_shouldNotReturnDeletedTask() {
        Task task = taskManager.createTask(new Task("Task", "Desc", 0, Status.NEW));
        taskManager.getTask(task.getId());
        taskManager.deleteTask(task.getId());

        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void shouldThrowWhenSubtaskReferencesItselfAsEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask invalidSubtask = new Subtask("Invalid", "Desc", epic.getId(), Status.NEW, epic.getId());

        assertThrows(IllegalArgumentException.class, () -> taskManager.createSubtask(invalidSubtask));
    }
}
