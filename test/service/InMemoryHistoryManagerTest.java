package service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import model.Task;
import model.TaskStatus;
import model.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class InMemoryHistoryManagerTest {
    HistoryManager historyManager;
    Task newTask, newTask2, newTask3;


    @BeforeEach
    void init() {
        historyManager = new InMemoryHistoryManager();
        newTask = new Task("The first task", "To do the first task", TaskStatus.NEW);
        newTask2 = new Task("The second task", "To do the second task", TaskStatus.NEW);
        newTask3 = new Task("The third task", "To do the third task", TaskStatus.NEW);
        newTask.setId(0);
        newTask2.setId(1);
        newTask3.setId(2);
    }

    @Test
    void shouldAddNewTaskToHistoryManager() {
        historyManager.add(newTask);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");
    }

    @Test
    void shouldFirstTaskIsSavedWhenSecondTaskIsAdded() {
        historyManager.add(newTask);
        historyManager.add(newTask2);
        final Task firstTaskFromHistory = historyManager.getHistory().getFirst();
        assertEquals(newTask.getName(), firstTaskFromHistory.getName(), "Имена задач не должны различаться.");
        assertEquals(newTask.getDescription(), firstTaskFromHistory.getDescription(), "Описания задач  " +
                "не должны различаться.");
        assertEquals(newTask.getTaskStatus(), firstTaskFromHistory.getTaskStatus(), "Статусы задачи " +
                "не должны различаться.");
    }

    @Test
    void shouldAddTasksToHistoryManagerGetTasks() {
        historyManager.add(newTask);
        historyManager.add(newTask2);
        historyManager.add(newTask3);
        assertEquals(3, historyManager.getHistory().size(), "Должно быть равным 3.");

    }

    @Test
    void shouldRemoveTaskFromHistoryManager() {
        historyManager.add(newTask);
        historyManager.add(newTask2);
        historyManager.add(newTask3);
        historyManager.remove(newTask2.getId());
        assertEquals(2, historyManager.getHistory().size(), "Должно быть равным 2.");
    }

    @Test
    void shouldRemoveTaskFromHistoryManagerBeforeAddingOfRepeatTask() {
        historyManager.add(newTask);
        historyManager.add(newTask2);
        historyManager.add(newTask3);
        historyManager.add(newTask);
        List<Task> tasksList = List.of(newTask2, newTask3, newTask);
        assertArrayEquals(tasksList.toArray(), historyManager.getHistory().toArray());

        tasksList = List.of(newTask2, newTask, newTask3);
        for (int i = 0; i < 2; i++) {
            historyManager.add(newTask3);
            assertArrayEquals(tasksList.toArray(), historyManager.getHistory().toArray());
        }
    }
}