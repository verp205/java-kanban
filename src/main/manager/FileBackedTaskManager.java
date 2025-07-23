package main.manager;

import main.enums.Priority;
import main.enums.Status;
import main.enums.TaskType;
import main.models.*;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    public final File file;
    private static final String HEADER = "id,type,name,status,description,startTime,duration,endTime,priority,epicId";

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public Task createTask(Task t) {
        Task res = super.createTask(t);
        save();
        return res;
    }

    @Override
    public Subtask createSubtask(Subtask s) {
        if (s.getId() == s.getEpicId()) {
            throw new IllegalArgumentException("Нельзя добавить подзадачу с id, совпадающим с эпиком");
        }
        Subtask res = super.createSubtask(s);
        save();
        return res;
    }


    @Override
    public Epic createEpic(Epic e) {
        Epic res = super.createEpic(e);
        save();
        return res;
    }

    @Override
    public void updateTask(Task t) {
        super.updateTask(t);
        save();
    }

    @Override
    public void updateSubtask(Subtask s) {
        super.updateSubtask(s);
        save();
    }

    @Override
    public void updateEpic(Epic e) {
        super.updateEpic(e);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    public void save() {
        try (Writer writer = new FileWriter(file)) {
            writer.write(HEADER + "\n");

            for (Task t : getAllTasks()) writer.write(t.toCsvString() + "\n");
            for (Epic e : getAllEpics()) writer.write(e.toCsvString() + "\n");
            for (Subtask s : getAllSubtasks()) writer.write(s.toCsvString() + "\n");

        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения в файл: " + e.getMessage(), e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) throws IOException {
        FileBackedTaskManager mgr = new FileBackedTaskManager(file);
        List<String> lines = Files.readAllLines(file.toPath());

        for (int i = 1; i < lines.size(); i++) {
            String[] f = lines.get(i).split(",", -1);
            if (f.length < 9) continue;

            int id = Integer.parseInt(f[0]);
            TaskType type = TaskType.valueOf(f[1]);
            String name = f[2];
            Status status = Status.valueOf(f[3]);
            String desc = f[4];
            LocalDateTime st = f[5].isEmpty() ? null : LocalDateTime.parse(f[5]);
            Duration dur = f[6].isEmpty() ? Duration.ZERO : Duration.parse(f[6]);
            LocalDateTime ed = f[7].isEmpty() ? null : LocalDateTime.parse(f[7]);
            Priority pr = f[8].isEmpty() ? null : Priority.valueOf(f[8]);
            String epicF = f.length > 9 ? f[9] : "";

            Task t;

            switch (type) {
                case TASK:
                    t = new Task(name, desc, id, status, type, st, dur);
                    break;
                case EPIC:
                    t = new Epic(name, desc, id, status);
                    break;
                case SUBTASK:
                    int epicId = Integer.parseInt(epicF);
                    t = new Subtask(name, desc, id, status, epicId, st, dur);
                    break;
                default:
                    continue;
            }

            t.setPriority(pr);
            t.setEndTime(ed);

            if (t instanceof Epic) {
                mgr.epics.put(t.getId(), (Epic) t);
            } else if (t instanceof Subtask) {
                mgr.subtasks.put(t.getId(), (Subtask) t);
                Epic epic = mgr.epics.get(((Subtask) t).getEpicId());
                if (epic != null) {
                    epic.addSubtask(t.getId());
                }
            } else {
                mgr.tasks.put(t.getId(), t);
            }

            mgr.sortedTasks.add(t);
            mgr.nextId = Math.max(mgr.nextId, t.getId() + 1);
        }

        for (Epic epic : mgr.epics.values()) {
            mgr.updateEpic(epic);
        }

        return mgr;
    }
}
