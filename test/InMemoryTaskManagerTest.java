import main.enums.Status;
import main.manager.InMemoryTaskManager;
import main.manager.TaskManager;
import main.models.Epic;
import main.models.Subtask;
import main.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    // ---- TASK TESTS ----

    @Test
    void createAndGetTask_shouldReturnCreatedTask() {
        Task task = new Task("Task", "Desc", 0, Status.NEW);
        Task created = taskManager.createTask(task);
        Task fetched = taskManager.getTask(created.getId());

        assertEquals(created, fetched);
    }

    @Test
    void getAllTasks_shouldReturnAllCreatedTasks() {
        taskManager.createTask(new Task("T1", "Desc", 0, Status.NEW));
        taskManager.createTask(new Task("T2", "Desc", 0, Status.NEW));

        ArrayList<Task> tasks = taskManager.getAllTasks();
        assertEquals(2, tasks.size());
    }

    @Test
    void updateTask_shouldModifyExistingTask() {
        Task task = taskManager.createTask(new Task("T1", "Desc", 0, Status.NEW));
        Task updated = new Task("Updated", "New Desc", task.getId(), Status.DONE);
        taskManager.updateTask(updated);

        Task result = taskManager.getTask(task.getId());
        assertEquals("Updated", result.getName());
        assertEquals(Status.DONE, result.getStatus());
    }

    @Test
    void deleteTask_shouldRemoveTask() {
        Task task = taskManager.createTask(new Task("Task", "Desc", 0, Status.NEW));
        taskManager.deleteTask(task.getId());
        assertNull(taskManager.getTask(task.getId()));
    }

    @Test
    void deleteAllTasks_shouldClearTaskList() {
        taskManager.createTask(new Task("Task1", "Desc", 0, Status.NEW));
        taskManager.createTask(new Task("Task2", "Desc", 0, Status.NEW));
        taskManager.deleteAllTasks();

        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    // ---- EPIC TESTS ----

    @Test
    void createAndGetEpic_shouldReturnCreatedEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Epic fetched = taskManager.getEpic(epic.getId());

        assertEquals(epic, fetched);
    }

    @Test
    void getAllEpics_shouldReturnAllCreatedEpics() {
        taskManager.createEpic(new Epic("E1", "Desc", 0, Status.NEW));
        taskManager.createEpic(new Epic("E2", "Desc", 0, Status.NEW));

        ArrayList<Epic> epics = taskManager.getAllEpics();
        assertEquals(2, epics.size());
    }

    @Test
    void updateEpic_shouldModifyExistingEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Epic updated = new Epic("Updated", "New Desc", epic.getId(), Status.DONE);
        taskManager.updateEpic(updated);

        Epic result = taskManager.getEpic(epic.getId());
        assertEquals("Updated", result.getName());
    }

    @Test
    void deleteEpic_shouldRemoveEpicAndSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask sub = taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));
        taskManager.deleteEpic(epic.getId());

        assertNull(taskManager.getEpic(epic.getId()));
        assertNull(taskManager.getSubtask(sub.getId()));
    }

    @Test
    void deleteAllEpics_shouldRemoveAllEpicsAndSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));
        taskManager.deleteAllEpics();

        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    // ---- SUBTASK TESTS ----

    @Test
    void createAndGetSubtask_shouldReturnCreatedSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask sub = taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));
        Subtask fetched = taskManager.getSubtask(sub.getId());

        assertEquals(sub, fetched);
    }

    @Test
    void getAllSubtasks_shouldReturnAllCreatedSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        taskManager.createSubtask(new Subtask("S1", "Desc", 0, Status.NEW, epic.getId()));
        taskManager.createSubtask(new Subtask("S2", "Desc", 0, Status.NEW, epic.getId()));

        ArrayList<Subtask> subtasks = taskManager.getAllSubtasks();
        assertEquals(2, subtasks.size());
    }

    @Test
    void updateSubtask_shouldModifySubtaskAndAffectEpicStatus() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask sub = taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));
        Subtask updated = new Subtask("Sub", "Desc", sub.getId(), Status.DONE, epic.getId());

        taskManager.updateSubtask(updated);
        assertEquals(Status.DONE, taskManager.getSubtask(sub.getId()).getStatus());
        assertEquals(Status.DONE, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void deleteSubtask_shouldRemoveSubtaskFromEpicAndHistory() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask sub = taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));

        taskManager.getSubtask(sub.getId());

        List<Task> historyBeforeDelete = taskManager.getHistory();
        assertTrue(historyBeforeDelete.stream().anyMatch(t -> t.getId() == sub.getId()));

        taskManager.deleteSubtask(sub.getId());

        assertNull(taskManager.getSubtask(sub.getId()));

        assertFalse(taskManager.getEpic(epic.getId()).getSubIds().contains(sub.getId()));

        List<Task> historyAfterDelete = taskManager.getHistory();
        assertFalse(historyAfterDelete.stream().anyMatch(t -> t.getId() == sub.getId()));
    }


    @Test
    void deleteAllSubtasks_shouldClearSubtaskListAndUpdateEpics() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        taskManager.createSubtask(new Subtask("S1", "Desc", 0, Status.NEW, epic.getId()));
        taskManager.deleteAllSubtasks();

        assertTrue(taskManager.getAllSubtasks().isEmpty());
        assertTrue(taskManager.getEpic(epic.getId()).getSubIds().isEmpty());
    }

    // ---- HISTORY TESTS ----

    @Test
    void getHistory_shouldReturnVisitedTasksOnly() {
        Task task = taskManager.createTask(new Task("T", "Desc", 0, Status.NEW));
        Epic epic = taskManager.createEpic(new Epic("E", "Desc", 0, Status.NEW));
        Subtask sub = taskManager.createSubtask(new Subtask("S", "Desc", 0, Status.NEW, epic.getId()));

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(sub.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size());
    }

    @Test
    void getHistory_shouldBeEmptyAfterAllTasksDeleted() {
        Task task = taskManager.createTask(new Task("T", "Desc", 0, Status.NEW));
        taskManager.getTask(task.getId());
        taskManager.deleteAllTasks();

        assertTrue(taskManager.getHistory().isEmpty());
    }
}
