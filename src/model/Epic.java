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

    public void computeDuration(HashMap<Integer, SubTask> subTasks) {
        Duration maxDuration = Duration.ZERO;

        for (Integer id: subTasks.keySet()) {
            maxDuration = maxDuration.plusMinutes(subTasks.get(id).getDuration().toMinutes());
        }

        duration = maxDuration;

        /*
            Метод проверен, перерасчет продолжительности эпика происходит после добавления и удаления подзадачи.
         */
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
        duration = Duration.ZERO;
        startTime = null;
        endTime = null;
    }

    public LocalDateTime getEndTime()  {
        return endTime;
    }

    public void computeStartTime(Map<Integer, SubTask> subTasks) {
        startTime = subTaskIds.stream()
                .map(subTasks::get)
                .filter(s -> s.getStartTime() != null)
                .map(SubTask::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    public void computeEndTime(Map<Integer, SubTask> subTasks) {
        endTime = subTaskIds.stream()
                .map(subTasks::get)
                .filter(s -> s.getEndTime() != null)
                .map(SubTask::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
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
