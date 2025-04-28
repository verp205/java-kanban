import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subIds = new ArrayList<>();

    public Epic(String name, String desc, int id, Status status) {
        super(name, desc, id, status);
    }

    public ArrayList<Integer> getSubIds() {
        return subIds;
    }

    public void addSubtask(int subId) {
        if (!subIds.contains(subId)) {
            subIds.add(subId);
        }
    }

    public void removeSubtask(int subId) {
        subIds.remove((Integer) subId);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", desc='" + getDesc() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subIds +
                '}';
    }

}

