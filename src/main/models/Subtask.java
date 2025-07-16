package main.models;

import main.enums.Status;
import main.enums.TaskType;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String desc, int id, Status status, int epicId) {
        super(name, desc, id, status, TaskType.SUBTASK);
        this.epicId = epicId;
    }

    @Override
    public String toCsvString() {
        return super.toCsvString() + "," + epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return super.toString() + ", epicId=" + epicId;
    }
}