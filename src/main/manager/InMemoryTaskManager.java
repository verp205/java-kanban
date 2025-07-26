package main.manager;

import main.models.*;
import main.enums.Status;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final NavigableSet<Task> sortedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );
    protected final HistoryManager historyManager = new InMemoryHistoryManager();
    protected int nextId = 1;

    // Метод проверки пересечения временных интервалов
    private boolean hasTimeOverlap(Task taskToCheck) {
        if (taskToCheck.getStartTime() == null || taskToCheck.getEndTime() == null) {
            return false;
        }

        return sortedTasks.stream()
                .filter(task -> task.getId() != taskToCheck.getId()) // Исключаем текущую задачу
                .filter(task -> task.getStartTime() != null && task.getEndTime() != null)
                .anyMatch(task -> isTimeOverlap(task, taskToCheck));
    }

    private boolean isTimeOverlap(Task task1, Task task2) {
        return !task1.getEndTime().isBefore(task2.getStartTime()) &&
                !task1.getStartTime().isAfter(task2.getEndTime());
    }

    // Общий метод добавления задач
    private <T extends Task> T safeAdd(T task, Map<Integer, T> storage) {
        if (hasTimeOverlap(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующей");
        }

        if (task.getId() == 0) {
            task.setId(nextId++);
        } else if (task.getId() >= nextId) {
            nextId = task.getId() + 1;
        }

        storage.put(task.getId(), task);
        if (task.getStartTime() != null) {
            sortedTasks.add(task);
        }
        return task;
    }

    // Методы создания задач
    @Override
    public Task createTask(Task task) {
        return safeAdd(task, tasks);
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask.getId() == subtask.getEpicId()) {
            throw new IllegalArgumentException("Подзадача не может ссылаться на себя");
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик не найден");
        }

        Subtask created = safeAdd(subtask, subtasks);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(created.getId());
        updateEpic(epic); // Теперь updateEpic будет обновлять и статус и время
        return created;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setPriority(null);
        return safeAdd(epic, epics);
    }

    // Методы удаления задач
    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            sortedTasks.remove(task);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            sortedTasks.remove(subtask);
            historyManager.remove(id);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id); // ← исправлено здесь
                updateEpicTime(epic.getId());
                updateEpicStatus(epic.getId());
            }
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            sortedTasks.remove(epic);
            historyManager.remove(id);

            // Удаляем все подзадачи эпика
            for (int subtaskId : new ArrayList<>(epic.getSubtaskIds())) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    sortedTasks.remove(subtask);
                    historyManager.remove(subtaskId);
                }
            }
        }
    }

    // Методы обновления задач
    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) return;

        Task oldTask = tasks.get(task.getId());
        sortedTasks.remove(oldTask);

        if (hasTimeOverlap(task)) {
            sortedTasks.add(oldTask);
            throw new IllegalArgumentException("Обновление создает пересечение по времени");
        }

        tasks.put(task.getId(), task);
        sortedTasks.add(task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) return;

        Subtask oldSubtask = subtasks.get(subtask.getId());
        sortedTasks.remove(oldSubtask);

        // Проверяем пересечение с другими задачами (кроме себя)
        if (hasTimeOverlap(subtask)) {
            sortedTasks.add(oldSubtask); // Восстанавливаем оригинальную задачу
            throw new IllegalArgumentException("Обновление создает пересечение по времени");
        }

        subtasks.put(subtask.getId(), subtask);
        sortedTasks.add(subtask);

        // Обновляем время эпика после изменения подзадачи
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.recalcTime(getEpicSubtasks(epic.getId()));
            // Обновляем эпик в sortedTasks
            sortedTasks.remove(epic);
            if (epic.getStartTime() != null) {
                sortedTasks.add(epic);
            }
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) return;

        Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());

        // Обновляем и статус и время
        updateEpicStatus(savedEpic.getId());
        updateEpicTime(savedEpic.getId());
    }

    private void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        sortedTasks.remove(epic);

        List<Subtask> subtasks = getEpicSubtasks(epicId);
        epic.recalcTime(subtasks);

        if (epic.getStartTime() != null) {
            sortedTasks.add(epic);
        }
    }

    // Методы получения задач с добавлением в историю
    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    // Методы массового удаления
    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            sortedTasks.remove(task);
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            sortedTasks.remove(epic);
            historyManager.remove(epic.getId());
        }
        epics.clear();

        for (Subtask subtask : subtasks.values()) {
            sortedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.removeSubtask(subtask.getId()); // Удаляет ID из subtaskIds
                updateEpicStatus(epicId);
                updateEpicTime(epicId);
            }
            historyManager.remove(subtask.getId());
            sortedTasks.remove(subtask);
        }
        subtasks.clear(); // Чистим карту
    }

    // Вспомогательные методы
    private List<Subtask> getEpicSubtasks(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic == null) return result;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> subtasks = getEpicSubtasks(epicId);
        if (subtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean hasNew = false;
        boolean hasInProgress = false;
        boolean hasDone = false;

        for (Subtask subtask : subtasks) {
            switch (subtask.getStatus()) {
                case NEW:
                    hasNew = true;
                    break;
                case IN_PROGRESS:
                    hasInProgress = true;
                    break;
                case DONE:
                    hasDone = true;
                    break;
            }
        }

        if (hasInProgress || (hasNew && hasDone)) {
            epic.setStatus(Status.IN_PROGRESS);
        } else if (hasNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.DONE);
        }
    }

    // Другие методы
    @Override
    public List<Task> getPrioritizedTasks() {
        List<Task> result = new ArrayList<>(sortedTasks); // задачи с временем

        // Добавляем задачи без времени
        for (Task task : tasks.values()) {
            if (task.getStartTime() == null) {
                result.add(task);
            }
        }

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getStartTime() == null) {
                result.add(subtask);
            }
        }

        for (Epic epic : epics.values()) {
            if (epic.getStartTime() == null) {
                result.add(epic);
            }
        }

        return result;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}