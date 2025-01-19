package service;


import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int HISTORY_MAX_SIZE = 10;
    private final List<Task> taskHistory;

    public InMemoryHistoryManager() {
        taskHistory = new ArrayList<>(HISTORY_MAX_SIZE);
    }

    @Override
    public void add(Task task) {
        if (taskHistory.size() < HISTORY_MAX_SIZE) {
            taskHistory.add(task);
        } else {
            Task nextTask;
            for (int i = 0; i < HISTORY_MAX_SIZE - 1; i++) {
                nextTask = taskHistory.get(i + 1);
                taskHistory.set(i, nextTask);
            }
            taskHistory.set(HISTORY_MAX_SIZE - 1, task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory;
    }

}
