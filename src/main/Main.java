package main;

import main.enums.Status;
import main.models.Task;
import main.models.Epic;
import main.models.Subtask;
import main.manager.TaskManager;
import main.manager.Managers;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 9, 0);

        // Создаем обычную задачу (используем анонимный класс)
        Task task1 = new Task("Покормить кота",
                "Дать корм утром",
                0, // ID будет установлен менеджером
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(15)) {

            @Override
            public String getTaskType() {
                return "TASK";
            }
        };
        manager.createTask(task1);

        // Создаем эпик (без времени выполнения)
        Epic epic1 = new Epic("Ремонт квартиры",
                "Полный ремонт всех комнат",
                0, // ID будет установлен менеджером
                Status.NEW);
        manager.createEpic(epic1);

        // Создаем подзадачи для эпика
        Subtask subtask1 = new Subtask("Демонтаж стен",
                "Снести старые перегородки",
                0, // ID будет установлен менеджером
                Status.IN_PROGRESS,
                epic1.getId(), // ID эпика
                baseTime.plusDays(1),
                Duration.ofHours(8));
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Укладка плитки",
                "Ванная комната",
                0, // ID будет установлен менеджером
                Status.NEW,
                epic1.getId(), // ID эпика
                baseTime.plusDays(2),
                Duration.ofHours(6));
        manager.createSubtask(subtask2);

        // Просматриваем задачи для заполнения истории
        System.out.println("Просматриваем задачи:");
        System.out.println(manager.getTask(task1.getId()));
        System.out.println(manager.getEpic(epic1.getId()));
        System.out.println(manager.getSubtask(subtask1.getId()));
        System.out.println(manager.getSubtask(subtask2.getId()));

        // Выводим все задачи
        printAllTasksWithDetails(manager);
    }

    private static void printAllTasksWithDetails(TaskManager manager) {
        System.out.println("\n=== Все задачи ===");

        System.out.println("\nОбычные задачи:");
        manager.getAllTasks().forEach(task -> {
            System.out.printf("[T%d] %s (%s) %s - %s%n",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getStartTime(),
                    task.getEndTime());
        });

        System.out.println("\nЭпики:");
        manager.getAllEpics().forEach(epic -> {
            System.out.printf("[E%d] %s (%s) %s - %s%n",
                    epic.getId(),
                    epic.getName(),
                    epic.getStatus(),
                    epic.getStartTime(),
                    epic.getEndTime());

            // Выводим подзадачи эпика
            manager.getAllSubtasks().stream()
                    .filter(sub -> sub.getEpicId() == epic.getId())
                    .forEach(sub -> System.out.printf("  [S%d] %s (%s) %s - %s%n",
                            sub.getId(),
                            sub.getName(),
                            sub.getStatus(),
                            sub.getStartTime(),
                            sub.getEndTime()));
        });

        System.out.println("\nИстория просмотров:");
        manager.getHistory().forEach(task -> {
            String type = (task instanceof Epic) ? "E" : (task instanceof Subtask) ? "S" : "T";
            System.out.printf("[%s%d] %s%n", type, task.getId(), task.getName());
        });
    }
}