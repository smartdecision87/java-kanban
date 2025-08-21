package service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    HistoryManager historyManager;
    //TaskManager taskManager;

    @BeforeEach
    void init() throws IOException {
        historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager(historyManager);
        task = new Task("A new task", "To do the new task", TaskStatus.NEW, Duration.ofMinutes(5),
                LocalDateTime.now().minusMinutes(20));
        epic = new Epic( "FOOD BUYING",
                "To buy food in a supermarket"
        );
        subTask = new SubTask("A call taxi",
                "A call taxi for getting to supermarket",
                TaskStatus.NEW,
                Duration.ofMinutes(7),
                LocalDateTime.now()
        );

    }

    @Test
    void shouldNotGetTasksConfluence() {
        Task task2 = new Task("A new task2", "To do the new task2", TaskStatus.NEW, Duration.ofMinutes(5),
                LocalDateTime.now().minusMinutes(15));
        taskManager.createTask(task2);
        System.out.println(String.format("Task : startTime - %s, endTime - %s", task.getStartTime(), task.getEndTime()));
        System.out.println(String.format("Task2: startTime - %s, endTime - %s", task2.getStartTime(), task2.getEndTime()));
        boolean isConfluence = taskManager.checkConfluence(task);
        assertFalse(isConfluence, "Задачи и подзадачи не должны пересекаться!");
    }

    @Test
    void shouldGetTasksConfluence() {
        Task task2 = new Task("A new task2", "To do the new task2", TaskStatus.NEW, Duration.ofMinutes(20),
                LocalDateTime.now().minusMinutes(15));
        taskManager.createTask(task2);
        boolean isConfluence = taskManager.checkConfluence(task);
        assertTrue(isConfluence, "Задачи и подзадачи должны пересекаться!");
    }

    @Test
    void shouldGetPrioritizedTaskSet() {
        Task task2 = new Task("A new task2", "To do the new task2", TaskStatus.NEW, Duration.ofMinutes(7),
                LocalDateTime.now().plusHours(4));
        Task task3 = new Task("A new task3", "To do the new task3", TaskStatus.NEW, Duration.ofMinutes(10),
                LocalDateTime.now().minusHours(2));
        Task createdTask2 = taskManager.createTask(task2);
        Task createdTask3 = taskManager.createTask(task3);
        Epic epicCreated = taskManager.createEpic(epic);
        SubTask createdSubTask = taskManager.createSubTask(subTask, epic.getId());
        Set<Task> sortedTaskSet = taskManager.getPrioritizedTasks();
        // Ожидаемый порядок задач и подзадач: { task3, task, task2 }
        Task[] expectedTaskSet = { createdTask3, createdSubTask, createdTask2 };
        assertArrayEquals(expectedTaskSet, sortedTaskSet.toArray(),
                "Задачи должны быть отсортированы по начальному времени");
    }
}