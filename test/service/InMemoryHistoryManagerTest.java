package service;

import java.util.List;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    HistoryManager historyManager;
    Task newTask, newTask2;


    @BeforeEach
    void init() {
        historyManager = new InMemoryHistoryManager();
        newTask = new Task("The first task", "To do the first task", TaskStatus.NEW);
        newTask2 = new Task("The second task", "To do the second task", TaskStatus.NEW);
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
}