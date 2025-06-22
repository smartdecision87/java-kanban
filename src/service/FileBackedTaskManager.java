package service;

import model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class FileBackedTaskManager extends InMemoryTaskManager  {
    private final File autosave;

    public FileBackedTaskManager(HistoryManager historyManager, File autosave) {
        super(historyManager);
        this.autosave = autosave;
    }

    public FileBackedTaskManager(File file) {
        this(Managers.createHistoryManager(), file);
    }

    public void save() {
        /*
            TO-DO:
            To save to CSV-file.
            Example:
            id,type,name,status,description,epic
            1,TASK,Task1,NEW,Description task1,
            2,EPIC,Epic2,DONE,Description epic2,
            3,SUBTASK,Sub Task2,DONE,Description sub task3,2
        */
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(autosave, StandardCharsets.UTF_8))) {
            bufferedWriter.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                bufferedWriter.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                bufferedWriter.write(toString(epic) + "\n");
            }
            for (SubTask subTask : getAllSubTasks()) {
                bufferedWriter.write(toString(subTask) + "\n");
            }
        } catch(IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл: " + autosave.getAbsolutePath());
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

        if (task instanceof SubTask) {
            epicIdStr = String.format("%d", ((SubTask) task).getEpicId());
        }

        return String.format("%d;%s;%s;%s;%s;%s",
                task.getId(),
                getTypeTask(task),
                task.getName(),
                task.getTaskStatus(),
                task.getDescription(),
                epicIdStr
        );

    }

    private static List<Object> fromString(String line) {
        // id,type,name,status,description,epic
        final String[] valuesFromLine = line.split(";");
        int id = Integer.parseInt(valuesFromLine[0]);
        String taskType = valuesFromLine[1];
        String name = valuesFromLine[2];
        TaskStatus status = valuesFromLine[3].equals("NEW") ? TaskStatus.NEW :
                valuesFromLine[3].equals("IN_PROGRESS") ? TaskStatus.IN_PROGRESS : TaskStatus.DONE;
        String description = valuesFromLine[4];

        List<Object> result = null;
        switch (taskType) {
            case "Task" -> {
                result = List.of(taskType, id, new Task(name, description, status));
            }
            case "Epic" -> {
                result = List.of(taskType, id, new Epic(name, description), status);
            }
            case "SubTask" -> {
                int epicId = Integer.parseInt(valuesFromLine[5]);
                result =  List.of(taskType, id, new SubTask(name, description, status), epicId);
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

class ManagerSaveException extends RuntimeException {
    ManagerSaveException(String message) {
        super(message);
    }
}