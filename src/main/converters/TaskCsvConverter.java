package main.converters;

import main.enums.Status;
import main.enums.TaskType;
import main.models.Epic;
import main.models.Subtask;
import main.models.Task;

public class TaskCsvConverter {
    public static Task fromCsvString(String csvLine) {
        String[] parts = csvLine.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String desc = parts[4];

        switch (type) {
            case TASK:
                return new Task(name, desc, id, status, type);
            case EPIC:
                return new Epic(name, desc, id, status);
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                return new Subtask(name, desc, id, status, epicId);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}