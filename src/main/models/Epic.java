package main.models;

import main.enums.Status;
import main.enums.TaskType;

import java.util.ArrayList;
import java.util.List;


public class Epic extends Task {
    private final List<Integer> subIds = new ArrayList<>();

    public Epic(String name, String desc, int id, Status status) {
        super(name, desc, id, status, TaskType.EPIC);
    }

    @Override
    public String toCsvString() {
        return super.toCsvString() + ",";
    }

    public void addSubtask(int subId) {
        if (subId != getId() && !subIds.contains(subId)) {
            subIds.add(subId);
        }
    }

    public void removeSubtask(int subId) {
        subIds.remove(Integer.valueOf(subId));
    }

    public List<Integer> getSubIds() {
        return subIds;
    }


    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", desc='" + getDesc() + '\'' +
                ", status=" + getStatus() +
                ", subIds=" + subIds +
                '}';
    }

}