package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.Task;
import model.Epic;
import model.SubTask;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, SubTask> subTasks;
    private int id;
    private final HistoryManager historyManager;
    public static final String RED = "\033[0;31m";
    public static final String GREEN = "\033[0;32m";
    public static final String RESET = "\033[0m";

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        this.historyManager = historyManager;
        id = 0;
    }

    private int generateId() {
        return ++id;
    }

    @Override
    public Task createTask(Task task) {
        int taskId = generateId();
        task.setId(taskId);
        tasks.put(taskId, task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.get(task.getId()) != null) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public Task getTask(int taskId) {
        Task task = tasks.get(taskId);

        if (task == null) {
            System.out.println("Задача с ID=" + taskId + " отстутствует в трекере задач!");
            return null;
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public void deleteTask(int taskId) {
        tasks.remove(taskId);
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<Task>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public Epic createEpic(Epic epic) {
        int epicId = generateId();
        epic.setId(epicId);
        epics.put(epicId, epic);
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        int epicId = epic.getId();
        if (epics.get(epicId) != null) {
            epics.put(epicId, epic);
        }
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = epics.get(epicId);

        if (epic == null) {
            System.out.println(RED + "Эпик с ID=" + epicId + " отстутствует в трекере задач!" + RESET);
            return null;
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void deleteEpic(int epicId) {

        for (int subTaskId : epics.get(epicId).getAllSubTaskIds()) {
            subTasks.remove(subTaskId);
        }
        epics.remove(epicId);
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<Epic>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        // To delete all subTasks of every Epic
        subTasks.clear();
        epics.clear();
    }

    @Override
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

    @Override
    public SubTask createSubTask(SubTask subTask, int epicId) {
        int subTaskId = generateId();
        subTask.setId(subTaskId);
        subTask.setEpicId(epicId);
        subTasks.put(subTaskId, subTask);
        Epic epic = epics.get(epicId);
        epic.addSubTask(subTaskId);
        epics.put(epicId, epic);
        epic.setTaskStatus(epic.computeEpicStatus(subTasks));
        return subTask;
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        int subTaskId = subTask.getId();
        if (subTasks.get(subTaskId) != null) {
            subTasks.put(subTaskId, subTask);
        }
        int epicId = subTask.getEpicId();
        Epic epic = epics.get(epicId);
        epic.setTaskStatus(epic.computeEpicStatus(subTasks));
    }

    @Override
    public SubTask getSubTask(int subTaskId) {
        SubTask subTask = subTasks.get(subTaskId);

        if (subTask == null) {
            System.out.println("Подзадача с ID=" + subTaskId + " отстутствует в трекере задач!");
            return null;
        }
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public void deleteSubTask(int subTaskId) {
        int epicId = subTasks.get(subTaskId).getEpicId();
        if (epicId != 0) {
            final Epic epic = epics.get(epicId);
            epic.deleteSubTask(subTaskId);
            subTasks.remove(subTaskId);
            epic.computeEpicStatus(subTasks);
        }
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        return new ArrayList<SubTask>(subTasks.values());
    }

    @Override
    public void deleteAllSubTasks() {
        for (Epic epic : epics.values()) {
            epic.deleteAllSubTasks();
            epic.computeEpicStatus(subTasks);
        }
        subTasks.clear();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
