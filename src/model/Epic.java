package model;

import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    final private ArrayList<Integer> subTaskIds = new ArrayList<>();

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + "'" +
                ", description='" + getDescription() + "'" +
                ", subTaskIds='" + subTaskIds + "'" +
                ", taskStatus='" + getTaskStatus() + "'" +
                '}';
    }

    public Epic(String name, String description, TaskStatus taskStatus) {
        super(name, description, taskStatus);
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
        TaskStatus taskStatus;

        if (subTaskIds.isEmpty()) {
            taskStatus = TaskStatus.NEW;
        } else {
            taskStatus = TaskStatus.DONE;
            for (Integer subTaskId : subTaskIds) {
                if (subTasks.get(subTaskId).getTaskStatus() != TaskStatus.DONE) {
                    taskStatus = TaskStatus.IN_PROGRESS;
                    break;
                }
            }
        }
        return taskStatus;
    }

    public void deleteAllSubTasks() {
        subTaskIds.clear();
    }
}
