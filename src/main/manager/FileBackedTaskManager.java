package main.manager;

import main.enums.Priority;
import main.enums.Status;
import main.models.*;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String HEADER = "id,type,name,status,description,startTime,duration,priority,epicId";

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public Task createTask(Task task) {
        Task created = super.createTask(task);
        save();
        return created;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic created = super.createEpic(epic);
        save();
        return created;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask.getId() == subtask.getEpicId()) {
            throw new IllegalArgumentException("Эпик не может быть подзадачей самого себя");
        }

        Subtask created = super.createSubtask(subtask);
        save();
        return created;
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(HEADER);
            writer.newLine();

            for (Epic epic : getAllEpics()) {
                writer.write(taskToCsv(epic));
                writer.newLine();
            }
            for (Task task : getAllTasks()) {
                writer.write(taskToCsv(task));
                writer.newLine();
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(taskToCsv(subtask));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    public void saveToFile() {
        save();
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // пропустить заголовок

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) break;

                String[] fields = line.split(",", -1); // split with empty values
                Task task = fromCsv(fields);
                if (task != null) {
                    manager.restoreTask(task);
                }
            }

            // Восстановление связей между эпиками и подзадачами
            for (Subtask sub : manager.subtasks.values()) {
                Epic epic = manager.epics.get(sub.getEpicId());
                if (epic != null) {
                    epic.addSubtask(sub.getId());
                }
            }

            // Пересчёт статусов и времени у эпиков
            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic.getId());
                epic.recalcTime(manager.getEpicSubtasks(epic.getId()));
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке из файла: " + file.getName(), e);
        }

        return manager;
    }

    private static Task fromCsv(String[] fields) {
        if (fields.length < 8) return null;

        int id = Integer.parseInt(fields[0]);
        String type = fields[1];
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        LocalDateTime startTime = fields[5].isEmpty() ? null : LocalDateTime.parse(fields[5]);
        Duration duration = fields[6].isEmpty() ? Duration.ZERO : Duration.parse(fields[6]);
        Priority priority = fields[7].isEmpty() ? null : Priority.valueOf(fields[7]);
        int epicId = (fields.length > 8 && !fields[8].isEmpty()) ? Integer.parseInt(fields[8]) : 0;

        switch (type) {
            case "TASK":
                Task task = new Task(name, description, id, status, startTime, duration);
                task.setPriority(priority);
                return task;
            case "EPIC":
                Epic epic = new Epic(name, description, id, status);
                epic.setPriority(priority);
                return epic;
            case "SUBTASK":
                Subtask subtask = new Subtask(name, description, id, status, epicId, startTime, duration);
                subtask.setPriority(priority);
                return subtask;
            default:
                return null;
        }
    }

    private String taskToCsv(Task task) {
        String base = String.format("%d,%s,%s,%s,%s",
                task.getId(),
                task.getTaskType(),
                task.getName(),
                task.getStatus(),
                task.getDescription());

        String timePart = (task.getStartTime() != null)
                ? String.format(",%s,%s", task.getStartTime(), task.getDuration().toString())
                : ",,";

        String priorityPart = (task.getPriority() != null)
                ? "," + task.getPriority()
                : ",";

        String epicIdPart = (task instanceof Subtask)
                ? "," + ((Subtask) task).getEpicId()
                : ",";  // обязательно добавляем запятую для выравнивания по полям

        return base + timePart + priorityPart + epicIdPart;
    }

    private void restoreTask(Task task) {
        switch (task.getTaskType()) {
            case "TASK":
                tasks.put(task.getId(), task);
                break;
            case "EPIC":
                epics.put(task.getId(), (Epic) task);
                break;
            case "SUBTASK":
                subtasks.put(task.getId(), (Subtask) task);
                break;
            default:
                throw new IllegalStateException("Неизвестный тип задачи: " + task.getTaskType());
        }

        if (task.getStartTime() != null) {
            sortedTasks.add(task);
        }

        nextId = Math.max(nextId, task.getId() + 1);
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

    public static class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}