package main.models;

import main.enums.Priority;
import main.enums.Status;
import main.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected int id;
    protected String name;
    protected String description;
    protected Status status;
    protected TaskType type;
    protected LocalDateTime startTime;
    protected Duration duration;
    protected LocalDateTime endTime;
    protected Priority priority;

    public Task(String name, String description, int id, Status status, TaskType type,
                LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.type = type;
        this.startTime = startTime;
        this.duration = duration == null ? Duration.ZERO : duration;
        if (startTime != null) {
            this.endTime = startTime.plus(this.duration);
        }
        this.priority = null;
    }

    // Геттеры и сеттеры

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Status getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        if (startTime != null && duration != null) {
            this.endTime = startTime.plus(duration);
        } else {
            this.endTime = null;
        }
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
        if (startTime != null && duration != null) {
            this.endTime = startTime.plus(duration);
        } else {
            this.endTime = null;
        }
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    public String toCsvString() {
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,",
                id,
                type.name(),
                name,
                status.name(),
                description,
                startTime == null ? "" : startTime.toString(),
                duration == null ? "" : duration.toString(),
                endTime == null ? "" : endTime.toString(),
                priority == null ? "" : priority.name()
        );
    }

    public TaskType getType() {
        return type;
    }
}
