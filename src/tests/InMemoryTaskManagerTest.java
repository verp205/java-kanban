package tests;

import enums.Status;
import manager.InMemoryTaskManager;
import manager.Managers;
import models.Epic;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    @Test
    void shouldAddAndFindTasksById() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task = new Task("Task", "Description", 1, Status.NEW);
        manager.createTask(task);

        assertEquals(task, manager.getTask(1), "Задача должна находиться по id");
    }

    @Test
    void tasksWithDifferentIdsShouldNotConflict() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", 2, Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        assertNotEquals(manager.getTask(1), manager.getTask(2),
                "Задачи с разными id не должны конфликтовать");
    }

    @Test
    void taskShouldRemainUnchangedWhenAddedToManager() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task original = new Task("Original", "Description", 1, Status.NEW);

        manager.createTask(original);
        Task fromManager = manager.getTask(1);

        assertEquals(original.getName(), fromManager.getName(), "Имя задачи не должно изменяться");
        assertEquals(original.getDesc(), fromManager.getDesc(), "Описание задачи не должно изменяться");
        assertEquals(original.getStatus(), fromManager.getStatus(), "Статус задачи не должен изменяться");
    }
}