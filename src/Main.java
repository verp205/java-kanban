import enums.Status;
import models.Task;
import manager.TaskManager;
import models.Epic;
import models.Subtask;


public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создание задач
        Task task1 = manager.createTask(new Task("Задача 1", "помыть посуду", 0, Status.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "вынести мусор", 0, Status.NEW));

        // Создание эпика 1 с двумя подзадачами
        Epic epic1 = manager.createEpic(new Epic("Купить авто", "накопить деньги", 0, Status.NEW));
        Subtask subtask1 = manager.createSubtask(new Subtask("Найти работу", "поиск работы", 0, Status.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Начать инвестировать", "создать портфель", 0, Status.NEW, epic1.getId()));

        // Создание эпика 2 с одной подзадачей
        Epic epic2 = manager.createEpic(new Epic("Купить дом", "приобрести дом без ипотеки", 0, Status.NEW));
        Subtask subtask3 = manager.createSubtask(new Subtask("Найти более оплачваемую работу", "найти работу с большой зп", 0, Status.NEW, epic2.getId()));

        // Печать всех задач, эпиков и подзадач
        System.out.println("--- Все задачи ---");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("--- Все эпики ---");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("--- Все подзадачи ---");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Изменение статусов
        task1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task1);

        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask2);

        subtask3.setStatus(Status.DONE);
        manager.updateSubtask(subtask3);

        // Печать всех задач, эпиков и подзадач после изменения статусов
        System.out.println("--- Обновлённые задачи ---");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("--- Обновлённые эпики ---");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("--- Обновлённые подзадачи ---");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Удаление задачи и эпика
        manager.deleteTask(task2.getId());
        manager.deleteEpic(epic2.getId());

        // Печать после удаления
        System.out.println("--- После удаления задачи и эпика ---");
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("Подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }
}
