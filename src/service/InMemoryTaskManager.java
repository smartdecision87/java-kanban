package service;

import java.util.*;
import java.util.stream.*;

import model.Task;
import model.Epic;
import model.SubTask;
import model.TaskStatus;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, SubTask> subTasks;
    protected final TreeSet<Task> sortedTasks;
    protected int id;
    protected final HistoryManager historyManager;
    public static final String RED = "\033[0;31m";
    public static final String GREEN = "\033[0;32m";
    public static final String RESET = "\033[0m";

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        sortedTasks = new TreeSet<>();
        this.historyManager = historyManager;
        id = 0;
    }

    private int generateId() {
        return ++id;
    }

    @Override
    public Task createTask(Task task) throws RuntimeException {
        int taskId = generateId();
        if (!checkConfluence(task)) {
            tasks.put(taskId, task);
        } else {
            throw new RuntimeException("Невозможно добавить подзадачу в менеджере задач. Задача имеет пересечение " +
                    "с другими задачами или подзадачами.");
        }
        task.setId(taskId);
        tasks.put(taskId, task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (!checkConfluence(task)) {
            tasks.put(task.getId(), task);
        } else {
            throw new RuntimeException("Невозможно обновить подзадачу в менеджере задач. Задача имеет пересечение " +
                    "с другими задачами или подзадачами.");
        }

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
        return epics.values().stream().toList();
    }

    @Override
    public void deleteAllEpics() {
        // To delete all subTasks of every Epic
        subTasks.clear();
        epics.clear();
    }

    @Override
    public List<SubTask> getEpicAllSubTasks(int epicId) {
        return subTasks.values().stream()
                .filter(s -> s.getEpicId() == epicId)
                .collect(Collectors.toList());
    }

    @Override
    public SubTask createSubTask(SubTask subTask, int epicId) throws RuntimeException {
        int subTaskId = generateId();
        if (!checkConfluence(subTask)) {
            subTasks.put(subTaskId, subTask);
        } else {
            throw new RuntimeException("Невозможно добавить подзадачу в менеджер задач. Подзадача имеет пересечение " +
                    "с другими задачами или подзадачами.");
        }
        subTask.setId(subTaskId);
        subTask.setEpicId(epicId);
        subTasks.put(subTaskId, subTask);
        Epic epic = epics.get(epicId);
        epic.addSubTask(subTaskId);
        epic.computeStartTime(subTasks);
        epic.computeDuration(subTasks);
        epic.computeEndTime(subTasks);
        epic.setTaskStatus(epic.computeEpicStatus(subTasks));
        return subTask;
    }

    @Override
    public void updateSubTask(SubTask subTask) throws RuntimeException {
        int subTaskId = subTask.getId();
        if (subTasks.get(subTaskId) == null) {
            throw new RuntimeException("Невозможно обновить подзадачу! Подзадача отсутствует в менеджере задач!");
        }
        if (!checkConfluence(subTask)) {
            subTasks.put(subTaskId, subTask);
        } else {
            throw new RuntimeException("Невозможно обновить подзадачу в менеджере задач. Подзадача пересекается " +
                    "с другими задачами или подзадачами.");
        }
        int epicId = subTask.getEpicId();
        Epic epic = epics.get(epicId);
        epic.setTaskStatus(epic.computeEpicStatus(subTasks));
        epic.computeStartTime(subTasks);
        epic.computeDuration(subTasks);
        epic.computeEndTime(subTasks);
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
            epic.computeEndTime(subTasks);
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
            epic.setTaskStatus(TaskStatus.NEW);
        }
        subTasks.clear();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public boolean checkConfluence(Task task) {
        // проверяем на пересечение по времени с другими задачами и подзадачами
        return Stream.concat(tasks.values().stream(), subTasks.values().stream())
                .filter(t -> t.getStartTime() != null
                            && t.getEndTime() != null
                            && t.getId() != task.getId()
                )
                .anyMatch(t -> t.getStartTime().isBefore(task.getEndTime())
                        && t.getEndTime().isAfter(task.getStartTime()));
    }

    public Set<Task> getPrioritizedTasks() {
        return Stream.concat(tasks.values().stream(), subTasks.values().stream())
                .filter(t -> t.getStartTime() != null)
                .sorted()
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
