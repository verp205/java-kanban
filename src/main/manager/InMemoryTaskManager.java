package main.manager;

import main.enums.Status;
import main.models.Task;
import main.models.Epic;
import main.models.Subtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;
    private final HistoryManager historyManager = Managers.getDefaultHistory();


    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }


    @Override
    public Task createTask(Task task) {
        Task newTask = new Task(task.getName(), task.getDesc(), nextId++, task.getStatus());
        tasks.put(newTask.getId(), newTask);
        return newTask;
    }

    @Override
    public void updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            Task existingTask = tasks.get(task.getId());
            existingTask.setName(task.getName());
            existingTask.setDesc(task.getDesc());
            existingTask.setStatus(task.getStatus());
        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        epics.clear();
        subtasks.clear();
    }


    @Override
    public Epic createEpic(Epic epic) {
        Epic newEpic = new Epic(epic.getName(), epic.getDesc(), nextId++, Status.NEW);
        epics.put(newEpic.getId(), newEpic);
        return newEpic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic != null && epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setName(epic.getName());
            existingEpic.setDesc(epic.getDesc());
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubIds()) {
                subtasks.remove(subId);
                historyManager.remove(subId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubIds().clear();
            updateEpicStatus(epic.getId());
        }
    }



    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask.getId() == subtask.getEpicId()) {
            throw new IllegalArgumentException("Подзадача не может быть своим же эпиком");
        }

        int subtaskId = nextId++;
        Subtask newSubtask = new Subtask(subtask.getName(), subtask.getDesc(), subtaskId, subtask.getStatus(), subtask.getEpicId());

        if (!epics.containsKey(newSubtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик с таким ID не найден");
        }

        subtasks.put(newSubtask.getId(), newSubtask);

        Epic epic = epics.get(newSubtask.getEpicId());
        epic.addSubtask(newSubtask.getId());
        updateEpicStatus(epic.getId());

        return newSubtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask != null && subtasks.containsKey(subtask.getId())) {
            Subtask existingSubtask = subtasks.get(subtask.getId());
            existingSubtask.setName(subtask.getName());
            existingSubtask.setDesc(subtask.getDesc());
            existingSubtask.setStatus(subtask.getStatus());
            updateEpicStatus(existingSubtask.getEpicId());
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubIds().remove(Integer.valueOf(id));
                updateEpicStatus(epic.getId());
            }
            historyManager.remove(id);
        }
    }



    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null || epic.getSubIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (int subId : epic.getSubIds()) {
            Subtask subtask = subtasks.get(subId);
            if (subtask == null) continue;

            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
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

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

}
