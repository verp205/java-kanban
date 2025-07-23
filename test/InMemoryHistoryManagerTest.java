import main.enums.Status;
import main.enums.TaskType;
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
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task(
                "Task 1",
                "desc",
                1,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.now(),
                Duration.ofHours(1)
        );

        task2 = new Task(
                "Task 2",
                "desc",
                2,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.now(),
                Duration.ofHours(1)
        );

        task3 = new Task(
                "Task 3",
                "desc",
                3,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.now(),
                Duration.ofHours(1)
        );

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
    }

    @Test
    void add_shouldAddNewTaskToHistory() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task task = new Task(
                "Task A",
                "desc",
                4,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.now(),
                Duration.ofHours(1)
        );
        manager.add(task);

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task, history.get(0), "Задача должна быть добавлена в конец истории");
    }

    @Test
    void add_shouldNotDuplicateSameTask() {
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Повторное добавление не должно создавать дубликат");
    }

    @Test
    void add_shouldMoveTaskToEndIfAlreadyExists() {
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать три уникальные задачи");

        assertEquals(task2, history.get(0), "task2 должна быть первой после перемещения task1");
        assertEquals(task3, history.get(1), "task3 должна быть второй");
        assertEquals(task1, history.get(2), "task1 должна быть перемещена в конец");
    }

    @Test
    void remove_shouldRemoveFromBeginningOfHistory() {
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(task2, task3), history, "После удаления task1 должны остаться task2 и task3");
    }

    @Test
    void remove_shouldRemoveFromMiddleOfHistory() {
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(task1, task3), history, "После удаления task2 должны остаться task1 и task3");
    }

    @Test
    void remove_shouldRemoveFromEndOfHistory() {
        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(task1, task2), history, "После удаления task3 должны остаться task1 и task2");
    }

    @Test
    void remove_shouldDoNothingIfIdNotFound() {
        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Удаление несуществующего id не должно влиять на историю");
    }
}
