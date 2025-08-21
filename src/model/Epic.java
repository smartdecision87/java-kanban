package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Epic extends Task {
    private final ArrayList<Integer> subTaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW, Duration.ZERO);
        endTime = LocalDateTime.now();
    }

    public ArrayList<Integer> getAllSubTaskIds() {
        return subTaskIds;
    }

    public Duration computeDuration(HashMap<Integer, SubTask> subTasks) {
        Duration epicDuration = Duration.ZERO;

        if (subTaskIds.isEmpty()) {
            throw new RuntimeException("Не существует ни одной подзадачи!");
        }

        for (Integer id: subTasks.keySet()) {
            epicDuration = epicDuration.plusMinutes(subTasks.get(id).getDuration().toMinutes());
        }

        return epicDuration;
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

    public LocalDateTime getEndTime()  {
        return endTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime computeEndTime(Map<Integer, SubTask> subTasks) throws RuntimeException {
        LocalDateTime maxEndTimeSubtask = null;

        if (subTaskIds.size() != 1) {
            for (int i = 0; i < subTaskIds.size() - 1; i++) {
                LocalDateTime endTimeCurrentSubtask = subTasks.get(subTaskIds.get(i)).getEndTime();
                LocalDateTime endTimeNextSubtask = subTasks.get(subTaskIds.get(i + 1)).getEndTime();
                maxEndTimeSubtask = endTimeCurrentSubtask.isAfter(endTimeNextSubtask)
                        ? endTimeCurrentSubtask : endTimeNextSubtask;
            }
        } else {
            maxEndTimeSubtask = subTasks.get(subTaskIds.get(0)).getEndTime();
        }

        return maxEndTimeSubtask;
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
