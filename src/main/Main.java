package main;

import main.enums.Status;
import main.enums.TaskType;
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

        // Создаем тестовые данные
        Task task1 = new Task("Задача 1",
                "Описание задачи 1",
                1,
                Status.NEW,
                TaskType.TASK,
                LocalDateTime.now(),
                Duration.ofHours(1));
        manager.createTask(task1);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1", 2, Status.NEW);
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", 3, Status.NEW, epic1.getId(), LocalDateTime.of(2025, 7, 1, 11, 0),
                Duration.ofMinutes(45));
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", 4, Status.IN_PROGRESS, epic1.getId(), LocalDateTime.of(2025, 7, 1, 11, 0),
                Duration.ofMinutes(45));
        manager.createSubtask(subtask2);

        // Просматриваем задачи, чтобы заполнить историю
        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        manager.getSubtask(subtask2.getId());

        // Выводим все задачи
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);

            // Получаем подзадачи эпика
            for (Subtask subtask : manager.getAllSubtasks()) {
                if (subtask.getEpicId() == epic.getId()) {
                    System.out.println("--> " + subtask);
                }
            }
        }

        System.out.println("Подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}