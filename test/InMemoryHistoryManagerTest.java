import main.enums.Status;
import main.manager.HistoryManager;
import main.manager.InMemoryHistoryManager;
import main.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void add_shouldAddNewTaskToHistory() {
        Task task = new Task("Task 1", "Description", 1, Status.NEW);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task, history.get(0), "Задача должна быть добавлена в конец истории");
    }

    @Test
    void add_shouldNotDuplicateSameTask() {
        Task task = new Task("Task", "Desc", 1, Status.NEW);
        historyManager.add(task);
        historyManager.add(task); // добавляется повторно

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Повторное добавление не должно создавать дубликат");
    }

    @Test
    void add_shouldMoveTaskToEndIfAlreadyExists() {
        Task task1 = new Task("Task 1", "Desc", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Desc", 2, Status.NEW);
        Task task3 = new Task("Task 3", "Desc", 3, Status.NEW);

        historyManager.add(task1); // task1 добавляется первой
        historyManager.add(task2); // затем task2
        historyManager.add(task3); // затем task3
        historyManager.add(task1); // task1 снова добавляется -> должна переместиться в конец

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать три уникальные задачи");

        // Проверяем, что порядок корректный: task2, task3, task1
        assertEquals(task2, history.get(0), "task2 должна быть первой после перемещения task1");
        assertEquals(task3, history.get(1), "task3 должна быть второй");
        assertEquals(task1, history.get(2), "task1 должна быть перемещена в конец");
    }


    @Test
    void remove_shouldRemoveTaskFromHistory() {
        Task task1 = new Task("Task 1", "Desc", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Desc", 2, Status.NEW);
        Task task3 = new Task("Task 3", "Desc", 3, Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "После удаления история должна содержать две задачи");
        assertFalse(history.contains(task2), "Удалённая задача не должна присутствовать в истории");
    }

    @Test
    void remove_shouldHandleNonExistentIdGracefully() {
        Task task = new Task("Task", "Desc", 1, Status.NEW);
        historyManager.add(task);
        historyManager.remove(999); // несуществующий ID

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Удаление несуществующей задачи не должно влиять на историю");
    }

    @Test
    void getHistory_shouldReturnEmptyIfNothingAdded() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой, если в неё ничего не добавлялось");
    }

}
