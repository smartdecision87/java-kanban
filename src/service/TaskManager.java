package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.Task;
import model.Epic;
import model.SubTask;
import model.TaskStatus;


public class TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, SubTask> subTasks;
    private int id = 0;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
    }

    private int generateId() {
        return ++id;
    }

    public Task createTask(Task task) {
        int taskId = generateId();
        task.setId(taskId);
        tasks.put(taskId, task);
        return task;
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

    public List<Task> getAllTasks() {
        return new ArrayList<Task>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Epic createEpic(Epic epic) {
        int epicId = generateId();
        epic.setId(epicId);
        epics.put(epicId, epic);
        return epic;
    }

    public void updateEpic(Epic epic) {
        int epicId = epic.getId();
        if (epics.get(epicId) != null) {
            epics.put(epicId, epic);
        }
    }

    public Epic getEpic(int epicId) {
        return epics.get(epicId);
    }

    public void deleteEpic(int epicId) {

        for (int subTaskId : epics.get(epicId).getAllSubTaskIds()) {
            subTasks.remove(subTaskId);
        }
        epics.remove(epicId);
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<Epic>(epics.values());
    }

    public void deleteAllEpics() {
        // To delete all subTasks of every Epic
        subTasks.clear();
        epics.clear();
    }

    public List<SubTask> getEpicAllSubTasks(int epicId) {
        ArrayList<SubTask> epicSubTasks = new ArrayList<>();

        Epic epic = epics.get(epicId);
        for (SubTask subTask : subTasks.values()) {
            if (subTask.getEpicId() == epicId) {
                epicSubTasks.add(subTask);
            }
        }
        return epicSubTasks;
    }

    public SubTask createSubTask(SubTask subTask, int epicId) {
        int subTaskId = generateId();
        subTask.setId(subTaskId);
        subTask.setEpicId(epicId);
        subTasks.put(subTaskId, subTask);
        Epic epic = epics.get(epicId);
        epic.setTaskStatus(epic.computeEpicStatus(subTasks));
        epic.addSubTask(subTaskId);
        epics.put(epicId, epic);
        return subTask;
    }

    public void updateSubTask(SubTask subTask) {
        int subTaskId = subTask.getId();
        if (subTasks.get(subTaskId) != null) {
            subTasks.put(subTaskId, subTask);
        }
        int epicId = subTask.getEpicId();
        Epic epic = epics.get(epicId);
        epic.computeEpicStatus(subTasks);
    }

    public SubTask getSubTask(int subTaskId) {
        return subTasks.get(subTaskId);
    }

    public void deleteSubTask(int subTaskId) {
        int epicId = subTasks.get(subTaskId).getEpicId();
        if (epicId != 0) {
            final Epic epic = epics.get(epicId);
            epic.deleteSubTask(subTaskId);
            subTasks.remove(subTaskId);
            epic.computeEpicStatus(subTasks);
        }
    }

    public List<SubTask> getAllSubTasks() {
        return new ArrayList<SubTask>(subTasks.values());
    }

    public void deleteAllSubTasks() {
        for (Epic epic : epics.values()) {
            epic.deleteAllSubTasks();
            epic.computeEpicStatus(subTasks);
        }
        subTasks.clear();
    }


}
