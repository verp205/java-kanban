import main.enums.Status;
import main.manager.HistoryManager;
import main.manager.InMemoryHistoryManager;
import main.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Task1", "Desc", 1, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        task2 = new Task("Task2", "Desc", 2, Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
    }

    @Test
    void shouldAddTasksToHistory() {
        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void shouldNotDuplicateTasks() {
        historyManager.add(task1);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    void shouldRemoveTasksFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }
}