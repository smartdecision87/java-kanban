package service;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.List;

public interface TaskManager {

    Task createTask(Task task);

    void updateTask(Task task);

    Task getTask(int taskId);

    void deleteTask(int taskId);

    List<Task> getAllTasks();

    void deleteAllTasks();

    Epic createEpic(Epic epic);

    void updateEpic(Epic epic);

    Epic getEpic(int epicId);

    void deleteEpic(int epicId);

    List<Epic> getAllEpics();

    void deleteAllEpics();

    List<SubTask> getEpicAllSubTasks(int epicId);

    SubTask createSubTask(SubTask subTask, int epicId);

    void updateSubTask(SubTask subTask);

    SubTask getSubTask(int subTaskId);

    void deleteSubTask(int subTaskId);

    List<SubTask> getAllSubTasks();

    void deleteAllSubTasks();

    List<Task> getHistory();

}
