package main.models;

import main.enums.Status;
import main.enums.TaskType;

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

    // Возвращаем неизменяемую копию списка
    public List<Integer> getSubtaskIds() {
        return List.copyOf(subtaskIds);
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
        LocalDateTime latestEnd = null;
        Duration totalDuration = Duration.ZERO;
        int validTasksCount = 0;

        for (Subtask subtask : subtasks) {
            if (subtask == null || subtask.getStartTime() == null || subtask.getDuration() == null) {
                continue;
            }

            validTasksCount++;
            LocalDateTime subtaskStart = subtask.getStartTime();
            LocalDateTime subtaskEnd = subtask.getEndTime();

            if (earliestStart == null || subtaskStart.isBefore(earliestStart)) {
                earliestStart = subtaskStart;
            }

            if (latestEnd == null || subtaskEnd.isAfter(latestEnd)) {
                latestEnd = subtaskEnd;
            }
        }

        if (validTasksCount > 0) {
            this.startTime = earliestStart;
            this.duration = Duration.between(earliestStart, latestEnd);
        } else {
            resetTime();
        }
    }

    private void resetTime() {
        this.startTime = null;
        this.duration = Duration.ZERO;
    }

    @Override
    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return subtaskIds.equals(epic.subtaskIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds);
    }



    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                ", startTime=" + startTime +
                ", duration=" + duration +
                ", endTime=" + getEndTime() +
                '}';
    }
}