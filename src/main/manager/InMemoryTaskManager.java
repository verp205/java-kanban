package main.manager;

import main.models.*;
import main.enums.Status;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final NavigableSet<Task> sortedTasks = new TreeSet<>(
            Comparator.<Task, LocalDateTime>comparing(t -> t.getStartTime(), Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );
    protected int nextId = 1;

    private boolean hasOverlap(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) return false;
        for (Task t : sortedTasks) {
            if (t.getId() == task.getId() ||
                    t.getStartTime() == null || t.getEndTime() == null) continue;
            if (!t.getEndTime().isBefore(task.getStartTime()) &&
                    !t.getStartTime().isAfter(task.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    private <T extends Task> T safeAdd(T task, Map<Integer, T> storage) {
        if (hasOverlap(task)) throw new IllegalArgumentException("Пересечение по времени");
        if (task.getId() == 0) {
            task.setId(nextId++);
        } else if (task.getId() >= nextId) {
            nextId = task.getId() + 1;
        }
        storage.put(task.getId(), task);
        sortedTasks.add(task);
        return task;
    }

    @Override
    public Task createTask(Task t) {
        return safeAdd(t, tasks);
    }

    @Override
    public Subtask createSubtask(Subtask s) {
        if (!epics.containsKey(s.getEpicId()))
            throw new IllegalArgumentException("Эпик не найден");
        Subtask added = safeAdd(s, subtasks);
        epics.get(s.getEpicId()).addSubtask(added.getId());
        updateEpic(epics.get(s.getEpicId()));
        return added;
    }

    @Override
    public Epic createEpic(Epic e) {
        e.setPriority(null);
        return safeAdd(e, epics);
    }

    @Override
    public void deleteTask(int id) {
        Task t = tasks.remove(id);
        if (t != null) sortedTasks.remove(t);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask s = subtasks.remove(id);
        if (s != null) {
            sortedTasks.remove(s);
            Epic e = epics.get(s.getEpicId());
            if (e != null) {
                e.removeSubtask(id);
                updateEpic(e);
            }
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic e = epics.remove(id);
        if (e != null) {
            sortedTasks.remove(e);
            for (int sid : new ArrayList<>(e.getSubIds())) {
                Subtask s = subtasks.remove(sid);
                if (s != null) sortedTasks.remove(s);
            }
        }
    }

    @Override
    public void updateTask(Task t) {
        if (tasks.containsKey(t.getId())) {
            sortedTasks.remove(tasks.get(t.getId()));
            if (hasOverlap(t)) throw new IllegalArgumentException("Пересечение");
            tasks.put(t.getId(), t);
            sortedTasks.add(t);
        }
    }

    @Override
    public void updateSubtask(Subtask s) {
        if (subtasks.containsKey(s.getId())) {
            sortedTasks.remove(subtasks.get(s.getId()));
            if (hasOverlap(s)) throw new IllegalArgumentException("Пересечение");
            subtasks.put(s.getId(), s);
            sortedTasks.add(s);
            updateEpic(epics.get(s.getEpicId()));
        }
    }

    @Override
    public void updateEpic(Epic e) {
        List<Subtask> subs = new ArrayList<>();
        for (int sid : e.getSubIds()) {
            if (subtasks.containsKey(sid)) {
                subs.add(subtasks.get(sid));
            }
        }
        e.recalcTime(subs);
        updateEpicStatus(e.getId());
    }

    private void updateEpicStatus(int eid) {
        Epic e = epics.get(eid);
        if (e.getSubIds().isEmpty()) {
            e.setStatus(Status.NEW);
            return;
        }
        boolean allNew = e.getSubIds().stream()
                .map(subtasks::get)
                .allMatch(s -> s.getStatus() == Status.NEW);
        boolean allDone = e.getSubIds().stream()
                .map(subtasks::get)
                .allMatch(s -> s.getStatus() == Status.DONE);
        e.setStatus(allDone ? Status.DONE : allNew ? Status.NEW : Status.IN_PROGRESS);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return tasks.values().stream()
                .filter(task -> task.getStartTime() != null && task.getDuration() != null)
                .sorted(Comparator.comparing(Task::getStartTime))
                .collect(Collectors.toList());
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
        return new ArrayList<>();
    }
}
