package service;

import java.util.ArrayList;
import java.util.HashMap;
import model.Task;
import model.Epic;
import model.SubTask;
import model.TaskStatus;


public class TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, SubTask> subTasks;
    private int id = 0;

    public TaskManager() {
        tasks = new HashMap<>(100);
        epics = new HashMap<>(100);
        subTasks = new HashMap<>(100);
    }

    private int generateId() {
        return ++id;
    }

    public int createTask(Task task) {
        int taskId = generateId();
        task.setId(taskId);
        tasks.put(taskId, task);
        return taskId;
    }

    public void updateTask(Task task) {
        if (tasks.get(task.getId()) != null) {
            tasks.put(task.getId(), task);
        }
    }

    public Task getTask(int taskId) {
        return tasks.get(taskId);
    }

    public void deleteTask(int taskId) {
        tasks.remove(taskId);
    }

    public void getAllTasks() {
        System.out.println(tasks);
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public int createEpic(Epic epic) {
        int epicId = generateId();
        epic.setId(epicId);
        epics.put(epicId, epic);
        return epicId;
    }

    public void updateEpic(Epic epic) {
        int epicId = epic.getId();
        if (epics.get(epicId) != null) {
            TaskStatus epicStatus = epics.get(epicId).computeEpicStatus(subTasks);
            Epic updatedEpic = new Epic(epic.getName(), epic.getDescription(), epicStatus);
            for (int subTaskId : epic.getAllSubTaskIds()) {
                updatedEpic.addSubTask(subTaskId);
            }
            epics.put(epicId, updatedEpic);
        }
    }

    public Epic getEpic(int epicId) {
        return epics.get(epicId);
    }

    public void deleteEpic(int epicId) {
        epics.remove(epicId);
    }

    public void getAllEpics() {
        System.out.println(epics);
    }

    public void deleteAllEpics() {
        // To delete all subTasks of every Epic
        subTasks.clear();
        epics.clear();
    }

    public int createSubTask(SubTask subTask) {
        int subTaskId = generateId();
        subTasks.put(subTaskId, subTask);
        return subTaskId;
    }

    public void updateSubTask(SubTask subTask) {
        int subTaskId = subTask.getId();
        if (subTasks.get(subTaskId) != null) {
            subTasks.put(subTaskId, subTask);
        }
    }

    public SubTask getSubTask(int subTaskId) {
        return subTasks.get(subTaskId);
    }

    public void deleteSubTask(int subTaskId) {
        tasks.remove(subTaskId);
    }

    public void getAllSubTasks() {
        System.out.println(subTasks);
    }

    public void deleteAllSubTasks() {
        subTasks.clear();
    }
}
