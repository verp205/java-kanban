import main.enums.Priority;
import main.enums.Status;
import main.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private Task task1;
    private Task task2;
    private LocalDateTime testStartTime;
    private Duration testDuration;

    @BeforeEach
    void setUp() {
        testStartTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        testDuration = Duration.ofHours(2);

        task1 = new Task("Task 1", "Description 1", 1, Status.NEW,
                testStartTime, testDuration);

        task2 = new Task("Task 2", "Description 2", 1, Status.IN_PROGRESS,
                testStartTime.plusDays(1), testDuration.plusHours(1));
    }

    @Test
    void tasksWithSameIdShouldBeEqual() {
        // Создаем анонимный подкласс Task с реализацией toCsvString()
        Task task1 = new Task("Task 1", "Description", 1, Status.NEW, null, null);

        Task task2 = new Task("Task 2", "Different description", 1, Status.IN_PROGRESS,
                LocalDateTime.now(), Duration.ofHours(1));

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    void taskFieldsShouldBeSetCorrectly() {
        assertEquals(1, task1.getId());
        assertEquals("Task 1", task1.getName());
        assertEquals("Description 1", task1.getDescription());
        assertEquals(Status.NEW, task1.getStatus());
        assertEquals(testStartTime, task1.getStartTime());
        assertEquals(testDuration, task1.getDuration());
        assertNull(task1.getPriority());
    }

    @Test
    void endTimeShouldBeCalculatedCorrectly() {
        LocalDateTime expectedEndTime = testStartTime.plus(testDuration);
        assertEquals(expectedEndTime, task1.getEndTime());
    }

    @Test
    void nullStartTimeShouldReturnNullEndTime() {
        task1.setStartTime(null);
        assertNull(task1.getEndTime());
    }

    @Test
    void settersShouldWorkCorrectly() {
        task1.setId(2);
        task1.setName("Updated Task");
        task1.setDescription("Updated Description");
        task1.setStatus(Status.DONE);
        task1.setPriority(Priority.HIGH);

        LocalDateTime newTime = LocalDateTime.now();
        Duration newDuration = Duration.ofMinutes(30);

        task1.setStartTime(newTime);
        task1.setDuration(newDuration);

        assertEquals(2, task1.getId());
        assertEquals("Updated Task", task1.getName());
        assertEquals("Updated Description", task1.getDescription());
        assertEquals(Status.DONE, task1.getStatus());
        assertEquals(Priority.HIGH, task1.getPriority());
        assertEquals(newTime, task1.getStartTime());
        assertEquals(newDuration, task1.getDuration());
    }

    @Test
    void nullDurationShouldBeConvertedToZero() {
        task1.setDuration(null);
        assertEquals(Duration.ZERO, task1.getDuration());
    }

    @Test
    void toStringShouldContainEssentialInfo() {
        String toString = task1.toString();
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name='Task 1'"));
        assertTrue(toString.contains("status=NEW"));
    }
}