package tests;

import main.manager.HistoryManager;
import main.manager.Managers;
import main.manager.TaskManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void getDefaultReturnsInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер задач не должен быть null");
    }

    @Test
    void getDefaultHistoryReturnsInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории не должен быть null");
    }
}