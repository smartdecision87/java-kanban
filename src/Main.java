import model.Epic;
import model.SubTask;
import service.TaskManager;
import model.Task;
import model.TaskStatus;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        final TaskManager taskManager = new TaskManager();
        // CREATE TASK
        final Task task = new Task("Erase",
                                    "To erase data in Database",
                                    TaskStatus.NEW);
        taskManager.createTask(task);
        System.out.println("Task is created: " + task.getId());
        System.out.println("Get task: " + task);
        // UPDATE TASK
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task);
        System.out.println("Update task: " + task);
        // DELETE TASK
        taskManager.deleteTask(task.getId());
        System.out.println(taskManager.getAllTasks());

        // CREATE EPIC AND ITS SUBTASKS
        final Epic epic = new Epic( "FOOD BUYING",
                                    "To buy food in a supermarket",
                                    TaskStatus.NEW
                                    );
        taskManager.createEpic(epic);
        final SubTask subTask = new SubTask("A call taxi",
                                            "A call taxi for getting to supermarket",
                                            TaskStatus.NEW
                                            );
        taskManager.createSubTask(subTask, epic.getId());
        final SubTask subTask2 = new SubTask("Carrot Buying",
                                            "To buy a few carrots",
                                            TaskStatus.NEW
                                            );
        taskManager.createSubTask(subTask2, epic.getId());
        taskManager.updateEpic(epic);
        System.out.println("Get Epic: " + epic);
        System.out.println("Get Subtask: " + subTask);
        System.out.println("Get Subtask2: " + subTask2);
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getEpicAllSubTasks(epic.getId()));
        // CHANGING EPIC STATUS
        subTask.setTaskStatus(TaskStatus.DONE);
        subTask2.setTaskStatus(TaskStatus.DONE);
        taskManager.updateEpic(epic);
        System.out.println("EPIC Status:");
        System.out.println("Get Epic: " + epic);
        System.out.println("Get Subtask: " + subTask);
        System.out.println("Get Subtask2: " + subTask2);

        // Delete all subtask of epic and check epicstatus
        taskManager.deleteSubTask(subTask.getId());
        taskManager.deleteSubTask(subTask2.getId());
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubTasks());
    }
}
