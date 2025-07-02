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
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Task 1", "Desc", 1, Status.NEW);
        task2 = new Task("Task 2", "Desc", 2, Status.NEW);
        task3 = new Task("Task 3", "Desc", 3, Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
    }

    @Test
    void add_shouldAddNewTaskToHistory() {
        HistoryManager manager = new InMemoryHistoryManager(); // отдельный экземпляр
        Task task = new Task("Task 1", "Description", 1, Status.NEW);
        manager.add(task);

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task, history.get(0), "Задача должна быть добавлена в конец истории");
    }

    @Test
    void add_shouldNotDuplicateSameTask() {
        historyManager.add(task1); // добавляется повторно

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Повторное добавление не должно создавать дубликат");
    }

    @Test
    void add_shouldMoveTaskToEndIfAlreadyExists() {
        historyManager.add(task1); // task1 снова добавляется -> должна переместиться в конец

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать три уникальные задачи");

        // Проверяем, что порядок корректный: task2, task3, task1
        assertEquals(task2, history.get(0), "task2 должна быть первой после перемещения task1");
        assertEquals(task3, history.get(1), "task3 должна быть второй");
        assertEquals(task1, history.get(2), "task1 должна быть перемещена в конец");
    }

    @Test
    void remove_shouldRemoveFromBeginningOfHistory() {
        historyManager.remove(1); // Удаляем task1 (начало)

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(task2, task3), history, "После удаления task1 должны остаться task2 и task3");
    }

    @Test
    void remove_shouldRemoveFromMiddleOfHistory() {
        historyManager.remove(2); // Удаляем task2 (середина)

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(task1, task3), history, "После удаления task2 должны остаться task1 и task3");
    }

    @Test
    void remove_shouldRemoveFromEndOfHistory() {
        historyManager.remove(3); // Удаляем task3 (конец)

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(task1, task2), history, "После удаления task3 должны остаться task1 и task2");
    }

    @Test
    void remove_shouldHandleNonExistentIdGracefully() {
        historyManager.remove(999); // несуществующий ID

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Удаление несуществующей задачи не должно влиять на историю");
    }

    @Test
    void getHistory_shouldReturnEmptyIfNothingAdded() {
        HistoryManager emptyManager = new InMemoryHistoryManager();
        List<Task> history = emptyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой, если в неё ничего не добавлялось");
    }
}
