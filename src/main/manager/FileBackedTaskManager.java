package main.manager;

import main.models.Task;
import main.models.Epic;
import main.models.Subtask;
import main.enums.Status;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Метод сохранения в файл
    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");

            // Сохраняем задачи
            for (Task task : getAllTasks()) {
                writer.write(taskToString(task) + "\n");
            }

            // Сохраняем эпики
            for (Epic epic : getAllEpics()) {
                writer.write(taskToString(epic) + "\n");
            }

            // Сохраняем подзадачи
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(taskToString(subtask) + "\n");
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private String taskToString(Task task) {
        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return String.format("%d,SUBTASK,%s,%s,%s,%d",
                    subtask.getId(),
                    subtask.getName(),
                    subtask.getStatus(),
                    subtask.getDesc(),
                    subtask.getEpicId());
        } else if (task instanceof Epic) {
            return String.format("%d,EPIC,%s,%s,%s,",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getDesc());
        } else {
            return String.format("%d,TASK,%s,%s,%s,",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getDesc());
        }
    }

    // Метод загрузки из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            for (int i = 1; i < lines.length; i++) { // Пропускаем заголовок
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String type = parts[1];
                String name = parts[2];
                Status status = Status.valueOf(parts[3]);
                String description = parts[4];

                switch (type) {
                    case "TASK":
                        Task task = new Task(name, description, id, status);
                        manager.createTask(task);
                        break;
                    case "EPIC":
                        Epic epic = new Epic(name, description, id, status);
                        manager.createEpic(epic);
                        break;
                    case "SUBTASK":
                        int epicId = Integer.parseInt(parts[5]);
                        Subtask subtask = new Subtask(name, description, id, status, epicId);
                        manager.createSubtask(subtask);
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки: " + e.getMessage());
        }

        return manager;
    }

    // Переопределенные методы с автосохранением
    @Override
    public Task createTask(Task task) {
        Task newTask = super.createTask(task);
        save();
        return newTask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic newEpic = super.createEpic(epic);
        save();
        return newEpic;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask newSubtask = super.createSubtask(subtask);
        save();
        return newSubtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}