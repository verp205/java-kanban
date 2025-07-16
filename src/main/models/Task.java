package main.models;

import main.enums.Status;
import main.enums.TaskType;
import java.util.Objects;

public class Task {
    private String name;
    private String desc;
    private int id;
    private Status status;
    private TaskType type;

    public Task(String name, String desc, int id, Status status, TaskType type) {
        this.name = name;
        this.desc = desc;
        this.id = id;
        this.status = status;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TaskType getType() {
        return type;
    }

    public String toCsvString() {
        return String.format("%d,%s,%s,%s,%s",
                id, type, name, status, desc);
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
        return Objects.hash(id);
    }


    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", status=" + status +
                '}';
    }

}

