package service;

import java.time.Duration;
import java.util.List;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class InMemoryHistoryManagerTest {
    HistoryManager historyManager;
    Task newTask, newTask2, newTask3;
    Task task1, task2, task3;

    @BeforeEach
    void init() {
        historyManager = new InMemoryHistoryManager();
        newTask = new Task("The first task", "To do the first task",
                            TaskStatus.NEW, Duration.ofMinutes(3));
        newTask2 = new Task("The second task", "To do the second task",
                            TaskStatus.NEW, Duration.ofMinutes(2));
        newTask3 = new Task("The third task", "To do the third task",
                            TaskStatus.NEW, Duration.ofMinutes(5));
        newTask.setId(0);
        newTask2.setId(1);
        newTask3.setId(2);

        task1 = new Task("Task1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(10));
        task2 = new Task("Task2", "Desc2", TaskStatus.NEW, Duration.ofMinutes(20));
        task3 = new Task("Task3", "Desc3", TaskStatus.NEW, Duration.ofMinutes(30));
        task1.setId(3);
        task2.setId(4);
        task3.setId(5);
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

    @Test
    void shouldHandleEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(),
                "История должна быть пустой при создании");
    }

    @Test
    void shouldRemoveFromHistoryBeginning() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task1.getId());

        assertEquals(1, historyManager.getHistory().size(),
                "В истории должна остаться одна задача");
        assertEquals(task2, historyManager.getHistory().get(0),
                "Оставшаяся задача должна быть task2");
    }

    @Test
    void shouldRemoveFromHistoryMiddle() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task2.getId());

        assertEquals(2, historyManager.getHistory().size(),
                "В истории должно остаться две задачи");
        assertEquals(List.of(task1, task3), historyManager.getHistory(),
                "Оставшиеся задачи должны быть task1 и task3");
    }

    @Test
    void shouldRemoveFromHistoryEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task2.getId());

        assertEquals(1, historyManager.getHistory().size(),
                "В истории должна остаться одна задача");
        assertEquals(task1, historyManager.getHistory().get(0),
                "Оставшаяся задача должна быть task1");
    }
}