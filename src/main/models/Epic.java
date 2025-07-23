package main.models;

import main.enums.Status;
import main.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subIds = new ArrayList<>();

    public Epic(String name, String desc, int id, Status status) {
        super(name, desc, id, status, TaskType.EPIC, null, Duration.ZERO);
    }

    public List<Integer> getSubIds() {
        return subIds;
    }

    public void addSubtask(int id) {
        subIds.add(id);
    }

    public void removeSubtask(int id) {
        subIds.remove((Integer) id);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public void recalcTime(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            this.startTime = null;
            this.endTime = null;
            this.duration = Duration.ZERO;
            return;
        }

        LocalDateTime start = null;
        LocalDateTime end = null;
        Duration totalDuration = Duration.ZERO;

        for (Subtask s : subtasks) {
            if (s.getStartTime() == null || s.getEndTime() == null) continue;
            if (start == null || s.getStartTime().isBefore(start)) start = s.getStartTime();
            if (end == null || s.getEndTime().isAfter(end)) end = s.getEndTime();
            totalDuration = totalDuration.plus(s.getDuration());
        }

        this.startTime = start;
        this.endTime = end;
        this.duration = totalDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Epic)) return false;
        Epic epic = (Epic) o;
        return this.id == epic.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
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
}
