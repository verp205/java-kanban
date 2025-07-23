package main.models;

import main.enums.Priority;
import main.enums.Status;
import main.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String desc, Status status, int epicId, LocalDateTime startTime, Duration duration) {
        super(name, desc, 0, status, TaskType.SUBTASK, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(String name, String desc, int id, Status status, int epicId, LocalDateTime startTime, Duration duration) {
        super(name, desc, id, status, TaskType.SUBTASK, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public Status getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subtask)) return false;
        if (!super.equals(o)) return false;  // сравним базовые поля Task
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public String toCsvString() {
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%d",
                id,
                type.name(),
                name,
                status.name(),
                description,
                startTime == null ? "" : startTime.toString(),
                duration == null ? "" : duration.toString(),
                endTime == null ? "" : endTime.toString(),
                priority == null ? "" : priority.name(),
                epicId
        );
    }
}
