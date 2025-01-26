package model;

import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    private final ArrayList<Integer> subTaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public ArrayList<Integer> getAllSubTaskIds() {
        return subTaskIds;
    }

    public void addSubTask(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void deleteSubTask(int subTaskId) {
        subTaskIds.remove(subTaskIds.indexOf(subTaskId));
    }

    public TaskStatus computeEpicStatus(HashMap<Integer, SubTask> subTasks) {

        if (subTaskIds.isEmpty()) {
            return TaskStatus.NEW;
        }

        int statusNewCounter = 0;
        int statusDoneCounter = 0;

        for (int subTaskId : subTaskIds) {
            if (subTasks.get(subTaskId).getTaskStatus() == TaskStatus.NEW) {
                statusNewCounter++;
            }
            if (subTasks.get(subTaskId).getTaskStatus() == TaskStatus.DONE) {
                statusDoneCounter++;
            }
        }

        if (statusNewCounter == subTaskIds.size()) return TaskStatus.NEW;
        if (statusDoneCounter == subTaskIds.size()) return TaskStatus.DONE;
        return TaskStatus.IN_PROGRESS;
    }

    public void deleteAllSubTasks() {
        subTaskIds.clear();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + "'" +
                ", description='" + getDescription() + "'" +
                ", subTaskIds='" + getAllSubTaskIds() + "'" +
                ", taskStatus='" + getTaskStatus() + "'" +
                '}';
    }
}
