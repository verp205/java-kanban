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
    protected int nextId = 1;

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    private final Map<Integer, Node> historyMap = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) return;
        remove(task.getId());
        linkLast(task);
    }

    private boolean hasTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) return false;

        return sortedTasks.stream()
                .filter(task -> task.getId() != newTask.getId())
                .filter(task -> task.getStartTime() != null && task.getEndTime() != null)
                .anyMatch(task -> !task.getEndTime().isBefore(newTask.getStartTime()) &&
                        !task.getStartTime().isAfter(newTask.getEndTime()));
    }

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

    @Override
    public Task createTask(Task task) {
        Task created = safeAdd(task, tasks);
        return created;
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
        epics.get(subtask.getEpicId()).addSubtask(created.getId());
        updateEpic(epics.get(subtask.getEpicId()));
        return created;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setPriority(null);
        return safeAdd(epic, epics);
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            sortedTasks.remove(task);
            remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            sortedTasks.remove(subtask);
            remove(id);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpic(epic);
            }
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            sortedTasks.remove(epic);
            remove(id);

            for (int subtaskId : new ArrayList<>(epic.getSubtaskIds())) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    sortedTasks.remove(subtask);
                    remove(subtaskId);
                }
            }
        }
    }

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

        if (hasTimeOverlap(subtask)) {
            sortedTasks.add(oldSubtask);
            throw new IllegalArgumentException("Обновление создает пересечение по времени");
        }

        subtasks.put(subtask.getId(), subtask);
        sortedTasks.add(subtask);

        // Добавляем обновление статуса эпика
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) return;

        Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
        updateEpicStatus(savedEpic.getId());
    }


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
        if (epic == null || epic.getSubtaskIds().isEmpty()) {
            if (epic != null) epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            if (subtask.getStatus() != Status.NEW) allNew = false;
            if (subtask.getStatus() != Status.DONE) allDone = false;

            // Если уже нашли и не-NEW и не-DONE, можно прервать цикл
            if (!allNew && !allDone) break;
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(sortedTasks);
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task getTask(int id) {
        return tasks.get(id);
    }

    @Override
    public void deleteAllTasks() {
        for (Task t : tasks.values()) {
            sortedTasks.remove(t);
        }
        tasks.clear();
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpic(int id) {
        return epics.get(id);
    }

    @Override
    public void deleteAllEpics() {
        for (Epic e : epics.values()) {
            sortedTasks.remove(e);
        }
        epics.clear();
        // Удаляем все подзадачи
        for (Subtask s : subtasks.values()) {
            sortedTasks.remove(s);
        }
        subtasks.clear();
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask s : subtasks.values()) {
            sortedTasks.remove(s);
            Epic e = epics.get(s.getEpicId());
            if (e != null) e.removeSubtask(s.getId());
        }
        subtasks.clear();
        // Обновить все эпики
        for (Epic e : epics.values()) {
            updateEpic(e);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> historyList = new ArrayList<>();
        Node current = head;
        while (current != null) {
            historyList.add(current.task);
            current = current.next;
        }
        return historyList;
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.get(id);
        if (node != null) {
            removeNode(node);
            historyMap.remove(id);
        }
    }

    private void linkLast(Task task) {
        Node newNode = new Node(task, tail, null);
        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode;
        historyMap.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }
}
