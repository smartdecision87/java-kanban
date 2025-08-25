package service;

import model.*;
import exception.ManagerSaveException;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class FileBackedTaskManager extends InMemoryTaskManager  {
    private final File autoSaveFile;

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        autoSaveFile = file;
    }

    public FileBackedTaskManager(File file) {
        this(Managers.createHistoryManager(), file);
    }

    public void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(autoSaveFile, StandardCharsets.UTF_8))) {
            bufferedWriter.write("id,type,name,status,description,epic,duration,startTime,endTime\n");
            for (Task task : getAllTasks()) {
                bufferedWriter.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                bufferedWriter.write(toString(epic) + "\n");
            }
            for (SubTask subTask : getAllSubTasks()) {
                bufferedWriter.write(toString(subTask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл: " + autoSaveFile.getAbsolutePath());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String[] lines = content.split("\n");

            if (lines.length <= 1) return manager; // Файл пуст или только заголовок

            // Чтение задач
            for (int i = 1; i < lines.length; i++) {
                List<Object> objects = fromString(lines[i]);
                if (objects == null) continue;
                String typeTask = (String)objects.getFirst();
                int id = (int)objects.get(1);

                switch (typeTask) {
                    case "Task" -> {
                        Task task = (Task) objects.get(2);
                        task.setId(id);
                        manager.tasks.put(id, task);
                    }
                    case "Epic" -> {
                        Epic epic = (Epic) objects.get(2);
                        epic.setId(id);
                        epic.setTaskStatus((TaskStatus)objects.get(3));
                        manager.epics.put(id, epic);
                    }
                    case "SubTask" -> {
                        SubTask subTask = (SubTask) objects.get(2);
                        subTask.setId(id);
                        int epicId = (int)objects.get(3);
                        subTask.setEpicId(epicId);
                        manager.subTasks.put(id, subTask);
                        Epic epic = manager.epics.get(epicId);
                        epic.addSubTask(id);
                        manager.updateSubTask(subTask);
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки файла" + file.getAbsolutePath());
        }

        return manager;
    }

    public String toString(Task task) {
        String epicIdStr = "";
        String startTimeStr = "";
        String endTimeStr = "";

        if (task instanceof SubTask) {
            epicIdStr = String.format("%d", ((SubTask) task).getEpicId());
        }

        if (task.getStartTime() != null) {
            startTimeStr = task.getStartTime().toString();
        }

        if (task.getEndTime() != null) {
            endTimeStr = task.getEndTime().toString();
        }

        return String.format("%d;%s;%s;%s;%s;%s;%s;%s;%s",
                task.getId(),
                getTypeTask(task),
                task.getName(),
                task.getTaskStatus(),
                task.getDescription(),
                epicIdStr,
                task.getDuration().toString(),
                startTimeStr,
                endTimeStr
        );

    }

    private static List<Object> fromString(String line) {
        // id,type,name,status,description,epic,duration,startTime,endTime
        final String[] valuesFromLine = line.split(";");
        int id = Integer.parseInt(valuesFromLine[0]);
        String taskType = valuesFromLine[1];
        String name = valuesFromLine[2];
        TaskStatus status = TaskStatus.valueOf(valuesFromLine[3]);
        String description = valuesFromLine[4];
        Duration duration = Duration.parse(valuesFromLine[6]);
        LocalDateTime startTime = LocalDateTime.parse((CharSequence) valuesFromLine[7]);

        List<Object> result = null;
        switch (taskType) {
            case "Task" -> {
                result = List.of(taskType, id, new Task(name, description, status, duration, startTime));
            }
            case "Epic" -> {
                LocalDateTime endTime = LocalDateTime.parse((CharSequence) valuesFromLine[8]);
                result = List.of(taskType, id, new Epic(name, description), status, duration, startTime, endTime);
            }
            case "SubTask" -> {
                int epicId = Integer.parseInt(valuesFromLine[5]);
                result =  List.of(taskType, id, new SubTask(name, description, status, duration, startTime), epicId);
            }
        }

        return result;
    }

    private String getTypeTask(Task task) {
        if (task instanceof SubTask) return "SubTask";
        else if (task instanceof Epic) return  "Epic";
        else return "Task";
    }

    @Override
    public Task createTask(Task task) {
        task = super.createTask(task);
        save();
        return task;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic = super.createEpic(epic);
        save();
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public SubTask createSubTask(SubTask subTask, int epicId) {
        subTask = super.createSubTask(subTask, epicId);
        save();
        return subTask;
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    @Override
    public void deleteSubTask(int id) {
        super.deleteSubTask(id);
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

}