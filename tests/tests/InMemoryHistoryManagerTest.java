package tests;

import main.enums.Status;
import main.manager.HistoryManager;
import main.manager.InMemoryHistoryManager;
import main.models.Task;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private final HistoryManager historyManager = new InMemoryHistoryManager();

    @Test
    void add_shouldAddTaskToHistory() {
        Task task = new Task("Task 1", "Description", 1, Status.NEW);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать 1 задачу");
        assertEquals(task, history.get(0), "Задача в истории должна совпадать с добавленной");
    }

    @Test
    void add_shouldNotAddNullTask() {
        historyManager.add(null);
        assertTrue(historyManager.getHistory().isEmpty(), "История должна остаться пустой");
    }

    @Test
    void add_shouldNotExceedMaxSize() {
        for (int i = 1; i <= 15; i++) {
            historyManager.add(new Task("Task " + i, "Description", i, Status.NEW));
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "История не должна превышать 10 элементов");
        assertEquals(15, history.get(0).getId(), "Последняя добавленная задача должна быть первой в списке");
        assertEquals(6, history.get(9).getId(), "Первая из добавленных задач должна быть удалена");
    }

    @Test
    void getHistory_shouldReturnEmptyListForEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void getHistory_shouldReturnImmutableList() {
        Task task = new Task("Task", "Description", 1, Status.NEW);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertThrows(UnsupportedOperationException.class, () -> history.add(task),
                "Список истории должен быть неизменяемым");
    }
}