import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import service.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;


public class Main {
    public static void main(String[] args) throws IOException {
        final TaskManager manager;
        final HttpTaskServer httpTaskServer;
        final Task task, task2;
        final SubTask subTask, subTask2, subTask3;
        final Epic epic, epic2;

        httpTaskServer = new HttpTaskServer();
        manager = httpTaskServer.getTaskManager();
        httpTaskServer.initialize();
        httpTaskServer.startServer();

        task = new Task("Erase",
                "To erase data in Database",
                TaskStatus.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.now());
        manager.createTask(task);

        task2 = new Task("Add",
                "To add data in Database",
                TaskStatus.NEW,
                Duration.ofMinutes(15),
                LocalDateTime.now().plusMinutes(10));
        manager.createTask(task2);

        epic = new Epic("FOOD BUYING","To buy food in a supermarket");
        manager.createEpic(epic);

        subTask = new SubTask("A call taxi",
                "A call taxi for getting to supermarket",
                TaskStatus.NEW,
                Duration.ofMinutes(7),
                LocalDateTime.now().plusMinutes(25)
        );
        manager.createSubTask(subTask, epic.getId());

        epic2 = new Epic("CAR BUYING","To buy a car");
        manager.createEpic(epic2);

        subTask2 = new SubTask("A call taxi",
                "A call taxi for getting to supermarket",
                TaskStatus.NEW,
                Duration.ofMinutes(12),
                LocalDateTime.now().plusMinutes(32)
        );
        manager.createSubTask(subTask2, epic2.getId());

        subTask3 = new SubTask("A call taxi",
                "A call taxi for getting to supermarket",
                TaskStatus.NEW,
                Duration.ofMinutes(5),
                LocalDateTime.now().plusMinutes(44)
        );
        manager.createSubTask(subTask3, epic2.getId());
    }
}
