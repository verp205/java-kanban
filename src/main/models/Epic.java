package main.models;

import main.enums.Status;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description, int id, Status status) {
        super(name, description, id, status, null, Duration.ZERO);
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtask(int id) {
        if (!subtaskIds.contains(id)) {
            subtaskIds.add(id);
        }
    }

    public void removeSubtask(int id) {
        subtaskIds.remove((Integer) id);
    }

    public void recalcTime(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            resetTime();
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestCalculatedEnd = null;
        boolean hasValidTasks = false;

        for (Subtask subtask : subtasks) {
            if (subtask == null || subtask.getStartTime() == null || subtask.getDuration() == null) {
                continue;
            }

            hasValidTasks = true;
            LocalDateTime subtaskStart = subtask.getStartTime();
            LocalDateTime subtaskEnd = subtaskStart.plus(subtask.getDuration());

            if (earliestStart == null || subtaskStart.isBefore(earliestStart)) {
                earliestStart = subtaskStart;
            }

            if (latestCalculatedEnd == null || subtaskEnd.isAfter(latestCalculatedEnd)) {
                latestCalculatedEnd = subtaskEnd;
            }
        }

        if (hasValidTasks) {
            this.startTime = earliestStart;
            this.duration = Duration.between(earliestStart, latestCalculatedEnd);
        } else {
            resetTime();
        }
    }

    private void resetTime() {
        this.startTime = null;
        this.duration = Duration.ZERO;
    }

    @Override
    public String getTaskType() {
        return "EPIC";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return id == epic.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                ", startTime=" + startTime +
                ", duration=" + duration +
                ", endTime=" + getEndTime() + // Используем геттер
                '}';
    }
}