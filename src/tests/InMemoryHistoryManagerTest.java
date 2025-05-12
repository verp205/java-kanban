package tests;

import enums.Status;
import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import models.Epic;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    @Test
    void shouldPreserveTaskDataInHistory() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task original = new Task("Original", "Description", 1, Status.NEW);

        historyManager.add(original);
        Task fromHistory = historyManager.getHistory().get(0);

        assertEquals(original.getName(), fromHistory.getName(), "Имя в истории должно сохраняться");
        assertEquals(original.getDesc(), fromHistory.getDesc(), "Описание в истории должно сохраняться");
        assertEquals(original.getStatus(), fromHistory.getStatus(), "Статус в истории должен сохраняться");
    }

    @Test
    void shouldNotAllowSubtaskToBeItsOwnEpic() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Epic epic = new Epic("Epic", "Description", 1, Status.NEW);
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", 2, Status.NEW, 2);
        assertThrows(IllegalArgumentException.class, () -> manager.createSubtask(subtask),
                "Подзадача не может быть своим же эпиком");
    }
}