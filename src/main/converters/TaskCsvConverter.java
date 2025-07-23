package main.converters;

import main.enums.Priority;
import main.enums.Status;
import main.enums.TaskType;
import main.models.Epic;
import main.models.Subtask;
import main.models.Task;

import java.time.Duration;
import java.time.LocalDateTime;

public class TaskCsvConverter {
    public static Task fromCsvString(String csvLine) {
        String[] parts = csvLine.split(",", -1); // -1 чтобы не терять пустые поля
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String desc = parts[4];
        LocalDateTime startTime = parts[5].isEmpty() ? null : LocalDateTime.parse(parts[5]);
        Duration duration = parts[6].isEmpty() ? Duration.ZERO : Duration.parse(parts[6]);
        LocalDateTime endTime = parts[7].isEmpty() ? null : LocalDateTime.parse(parts[7]);
        Priority priority = parts[8].isEmpty() ? null : Priority.valueOf(parts[8]);
        String epicIdStr = parts.length > 9 ? parts[9] : "";

        Task task;

        switch (type) {
            case TASK:
                task = new Task(name, desc, id, status, type, startTime, duration);
                break;
            case EPIC:
                task = new Epic(name, desc, id, status);
                break;
            case SUBTASK:
                int epicId = Integer.parseInt(epicIdStr);
                task = new Subtask(name, desc, id, status, epicId, startTime, duration);
                break;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }

        task.setPriority(priority);
        task.setEndTime(endTime);

        return task;
    }
}