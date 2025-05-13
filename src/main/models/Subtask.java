package main.models;

import main.enums.Status;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String desc, int id, Status status, int epicId) {
        super(name, desc, id, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", desc='" + getDesc() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }
}