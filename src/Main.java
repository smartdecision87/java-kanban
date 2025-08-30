import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import service.*;

import java.time.Duration;


public class Main {

    public static void main(String[] args) {

        final TaskManager taskManager = Managers.getDefault();

        // CREATE TASK
        final Task task = new Task("Erase",
                                    "To erase data in Database",
                                    TaskStatus.NEW,
                                    Duration.ofMinutes(10));
        taskManager.createTask(task);
        System.out.println("Task is created: " + task.getId());
        System.out.println("Get task: " + task);
        // UPDATE TASK
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task);
        System.out.println("Update task: " + task);
        // DELETE TASK
//       taskManager.deleteTask(task.getId());
        System.out.println(taskManager.getAllTasks());

        // CREATE EPIC AND ITS SUBTASKS
        final Epic epic = new Epic("FOOD BUYING",
                                    "To buy food in a supermarket"
                                    );
        taskManager.createEpic(epic);
        final SubTask subTask = new SubTask("A call taxi",
                                            "A call taxi for getting to supermarket",
                                            TaskStatus.NEW,
                                            Duration.ofMinutes(12)
                                            );
        taskManager.createSubTask(subTask, epic.getId());
        final SubTask subTask2 = new SubTask("Carrot Buying",
                                            "To buy a few carrots",
                                            TaskStatus.NEW,
                                            Duration.ofMinutes(14)
                                            );
        taskManager.createSubTask(subTask2, epic.getId());
        System.out.println("Get Epic: " + epic);
        System.out.println("Get Subtask: " + subTask);
        System.out.println("Get Subtask2: " + subTask2);
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getEpicAllSubTasks(epic.getId()));
        // CHANGING EPIC STATUS
        System.out.println(InMemoryTaskManager.GREEN + "\t\tCHANGING EPIC STATUS" + InMemoryTaskManager.RESET);
        subTask.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask);
        subTask2.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask2);
        System.out.println("EPIC Status:");
        System.out.println("Get Epic: " + epic);
        System.out.println("Get Subtask: " + subTask);
        System.out.println("Get Subtask2: " + subTask2);

        // Delete all subtask of epic and check epicstatus
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubTasks());

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubTask(subTask2.getId());
        taskManager.getSubTask(subTask.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getTask(task.getId());
        taskManager.getSubTask(subTask.getId());
        taskManager.getSubTask(subTask.getId());
        taskManager.getTask(task.getId());
        taskManager.getSubTask(subTask2.getId());
        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());

        int i = 1;
        for (Task t : taskManager.getHistory()) {
            System.out.println(i++ + " " + t);
        }
    }
}
