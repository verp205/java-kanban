import main.enums.Status;
import main.enums.TaskType;
import main.models.Task;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task A", "desc", 0, Status.NEW, TaskType.TASK,
                LocalDateTime.now(), Duration.ofHours(1));
        Task task2 = new Task("Task B", "desc", 0, Status.NEW, TaskType.TASK,
                LocalDateTime.now(), Duration.ofHours(1));

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }
}