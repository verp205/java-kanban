import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Task createTask(Task task) {
        Task newTask = new Task(task.getName(), task.getDesc(), nextId++, task.getStatus());
        tasks.put(newTask.getId(), newTask);
        return newTask;
    }

    public void updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            Task existingTask = tasks.get(task.getId());
            existingTask.setName(task.getName());
            existingTask.setDesc(task.getDesc());
            existingTask.setStatus(task.getStatus());
        }
    }

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Epic createEpic(Epic epic) {
        Epic newEpic = new Epic(epic.getName(), epic.getDesc(), nextId++, Status.NEW);
        epics.put(newEpic.getId(), newEpic);
        return newEpic;
    }

    public void updateEpic(Epic epic) {
        if (epic != null && epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setName(epic.getName());
            existingEpic.setDesc(epic.getDesc());
        }
    }

    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubIds()) {
                subtasks.remove(subId);
            }
        }
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public Subtask createSubtask(Subtask subtask) {
        Subtask newSubtask = new Subtask(subtask.getName(), subtask.getDesc(), nextId++, subtask.getStatus(), subtask.getEpicId());
        subtasks.put(newSubtask.getId(), newSubtask);

        Epic epic = epics.get(newSubtask.getEpicId());
        epic.addSubtask(newSubtask.getId());
        updateEpicStatus(epic.getId());

        return newSubtask;
    }

    public void updateSubtask(Subtask subtask) {
        if (subtask != null && subtasks.containsKey(subtask.getId())) {
            Subtask existingSubtask = subtasks.get(subtask.getId());
            existingSubtask.setName(subtask.getName());
            existingSubtask.setDesc(subtask.getDesc());
            existingSubtask.setStatus(subtask.getStatus());
            updateEpicStatus(existingSubtask.getEpicId());
        }
    }

    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic.getId());
            }
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
}
