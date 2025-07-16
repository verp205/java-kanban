import main.enums.Status;
import main.enums.TaskType;
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
        Task task = new Task("Task", "Desc", 0, Status.NEW, TaskType.TASK);
        Task created = taskManager.createTask(task);
        Task fetched = taskManager.getTask(created.getId());

        assertEquals(created, fetched);
        assertEquals(TaskType.TASK, fetched.getType());
    }

    @Test
    void getAllTasks_shouldReturnAllCreatedTasks() {
        taskManager.createTask(new Task("T1", "Desc", 0, Status.NEW, TaskType.TASK));
        taskManager.createTask(new Task("T2", "Desc", 0, Status.NEW, TaskType.TASK));

        ArrayList<Task> tasks = taskManager.getAllTasks();
        assertEquals(2, tasks.size());
        assertEquals(TaskType.TASK, tasks.get(0).getType());
    }

    @Test
    void updateTask_shouldModifyExistingTask() {
        Task task = taskManager.createTask(new Task("T1", "Desc", 0, Status.NEW, TaskType.TASK));
        Task updated = new Task("Updated", "New Desc", task.getId(), Status.DONE, TaskType.TASK);
        taskManager.updateTask(updated);

        Task result = taskManager.getTask(task.getId());
        assertEquals("Updated", result.getName());
        assertEquals(Status.DONE, result.getStatus());
        assertEquals(TaskType.TASK, result.getType());
    }

    @Test
    void deleteTask_shouldRemoveTask() {
        Task task = taskManager.createTask(new Task("Task", "Desc", 0, Status.NEW, TaskType.TASK));
        taskManager.deleteTask(task.getId());
        assertNull(taskManager.getTask(task.getId()));
    }

    @Test
    void deleteAllTasks_shouldClearTaskList() {
        taskManager.createTask(new Task("Task1", "Desc", 0, Status.NEW, TaskType.TASK));
        taskManager.createTask(new Task("Task2", "Desc", 0, Status.NEW, TaskType.TASK));
        taskManager.deleteAllTasks();

        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    // ---- EPIC TESTS ----

    @Test
    void createAndGetEpic_shouldReturnCreatedEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Epic fetched = taskManager.getEpic(epic.getId());

        assertEquals(epic, fetched);
        assertEquals(TaskType.EPIC, fetched.getType());
    }

    @Test
    void getAllEpics_shouldReturnAllCreatedEpics() {
        taskManager.createEpic(new Epic("E1", "Desc", 0, Status.NEW));
        taskManager.createEpic(new Epic("E2", "Desc", 0, Status.NEW));

        ArrayList<Epic> epics = taskManager.getAllEpics();
        assertEquals(2, epics.size());
        assertEquals(TaskType.EPIC, epics.get(0).getType());
    }

    @Test
    void updateEpic_shouldModifyExistingEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Epic updated = new Epic("Updated", "New Desc", epic.getId(), Status.NEW);
        taskManager.updateEpic(updated);

        Epic result = taskManager.getEpic(epic.getId());
        assertEquals("Updated", result.getName());
        assertEquals(TaskType.EPIC, result.getType());
    }

    @Test
    void deleteEpic_shouldRemoveEpicAndSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask sub = taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));
        taskManager.deleteEpic(epic.getId());

        assertNull(taskManager.getEpic(epic.getId()));
        assertNull(taskManager.getSubtask(sub.getId()));
    }

    // ---- SUBTASK TESTS ----

    @Test
    void createAndGetSubtask_shouldReturnCreatedSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask sub = taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));
        Subtask fetched = taskManager.getSubtask(sub.getId());

        assertEquals(sub, fetched);
        assertEquals(TaskType.SUBTASK, fetched.getType());
    }

    @Test
    void updateSubtask_shouldModifySubtaskAndAffectEpicStatus() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", 0, Status.NEW));
        Subtask sub = taskManager.createSubtask(new Subtask("Sub", "Desc", 0, Status.NEW, epic.getId()));
        Subtask updated = new Subtask("Sub", "Desc", sub.getId(), Status.DONE, epic.getId());

        taskManager.updateSubtask(updated);
        assertEquals(Status.DONE, taskManager.getSubtask(sub.getId()).getStatus());
        assertEquals(Status.DONE, taskManager.getEpic(epic.getId()).getStatus());
        assertEquals(TaskType.SUBTASK, taskManager.getSubtask(sub.getId()).getType());
    }

    // ---- HISTORY TESTS ----

    @Test
    void getHistory_shouldReturnVisitedTasksOnly() {
        Task task = taskManager.createTask(new Task("T", "Desc", 0, Status.NEW, TaskType.TASK));
        Epic epic = taskManager.createEpic(new Epic("E", "Desc", 0, Status.NEW));
        Subtask sub = taskManager.createSubtask(new Subtask("S", "Desc", 0, Status.NEW, epic.getId()));

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(sub.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(TaskType.TASK, history.get(0).getType());
        assertEquals(TaskType.EPIC, history.get(1).getType());
        assertEquals(TaskType.SUBTASK, history.get(2).getType());
    }
}