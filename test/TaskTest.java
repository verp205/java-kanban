import main.enums.Status;
import main.enums.TaskType;
import main.models.Task;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW, TaskType.TASK);
        Task task2 = new Task("Task 2", "Description 2", 1, Status.DONE, TaskType.TASK);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }
}