import model.Epic;
import model.SubTask;
import service.TaskManager;
import model.Task;
import model.TaskStatus;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        int taskId = taskManager.createTask(new Task("Erase", "To erase data in Database", TaskStatus.NEW));
        System.out.println("Task is created: " + taskId);

        Task taskFromManager = taskManager.getTask(taskId);
        System.out.println("Get task: " + taskFromManager);

        taskFromManager.setName("Add");
        taskManager.updateTask(taskFromManager);
        System.out.println("Update task: " + taskFromManager);
        int epicId = taskManager.createEpic(new Epic("Travel to Chili", "Travelling", TaskStatus.NEW));
        Epic epicFromManager = taskManager.getEpic(epicId);
        System.out.println("Get Epic: " + epicFromManager);
        int subTaskId = taskManager.createSubTask(new SubTask("Getting a car", "getting to train by car", TaskStatus.NEW));
        epicFromManager.addSubTask(subTaskId);
        taskManager.updateEpic(epicFromManager);
        epicFromManager = taskManager.getEpic(epicId);
        epicFromManager.deleteSubTask(subTaskId);
        taskManager.updateEpic(epicFromManager);
        //taskManager.deleteAllEpics();
        //taskManager.getAllSubTasks();
    //    taskManager.getAllTasks();

//       taskManager.deleteTask(taskFromManager.getId());
//        System.out.println("Delete task:" + taskFromManager);
    }
}
