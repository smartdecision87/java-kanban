package service;

import exception.NotFoundException;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected HistoryManager historyManager;
    protected Task task;
    protected Epic epic;
    protected SubTask subTask;

    @BeforeEach
    abstract void init() throws IOException;

    @Test
    void shouldCreateTask() {
        Task createdTask = taskManager.createTask(task);
        assertNotNull(createdTask, "Задача должна быть создана");
        assertEquals(task.getId(), createdTask.getId(), "Идентификаторы задач не должны различаться!");
        assertEquals(task.getName(), createdTask.getName(), "Имена задач не должны различаться.");
        assertEquals(task.getTaskStatus(), createdTask.getTaskStatus(), "Статусы задач не должны различаться.");
    }

    @Test
    void shouldUpdateTask() {
        taskManager.createTask(task);
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        task.setName("Обновленное имя");
        taskManager.updateTask(task);
        Task updatedTask = taskManager.getTask(task.getId());
        assertEquals(task.getId(), updatedTask.getId(), "Идентификаторы задач не должны различаться!");
        assertEquals(task.getName(), updatedTask.getName(), "Имена задач не должны различаться.");
        assertEquals(task.getTaskStatus(), updatedTask.getTaskStatus(), "Статусы задач не должны различаться.");
    }

    @Test
    void shouldGetTask() {
        taskManager.createTask(task);
        Task retrievedTask = taskManager.getTask(task.getId());
        assertNotNull(retrievedTask, "Задача должна быть получена");
        assertEquals(task.getId(), retrievedTask.getId(), "Идентификаторы задач не должны различаться!");
        assertEquals(task.getName(), retrievedTask.getName(), "Имена задач не должны различаться.");
        assertEquals(task.getTaskStatus(), retrievedTask.getTaskStatus(), "Статусы задач не должны различаться.");
    }

    @Test
    void shouldDeleteTask() {
        Task createdTask = taskManager.createTask(task);
        taskManager.deleteTask(createdTask.getId());
        assertThrows(NotFoundException.class,
                () -> taskManager.getTask(createdTask.getId()),
                "Должно выбрасываться исключение при работе с несуществующей задачей");
    }

    @Test
    void shouldGetAllTasks() {
        taskManager.createTask(task);
        Task task2 = new Task("Другая задача", "Описание другой задачи", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(20));
        taskManager.createTask(task2);
        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(2, tasks.size(), "Должно быть 2 задачи");
        assertTrue(tasks.contains(task), "Первая задача должна быть в списке");
        assertTrue(tasks.contains(task2), "Вторая задача должна быть в списке");
    }

    @Test
    void shouldDeleteAllTasks() {
        taskManager.createTask(task);
        taskManager.createTask(new Task("Другая задача", "Описание", TaskStatus.NEW,
                Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(20)));
        taskManager.deleteAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty(), "Все задачи должны быть удалены");
    }

    @Test
    void shouldCreateEpic() {
        Epic createdEpic = taskManager.createEpic(epic);
        assertNotNull(createdEpic, "Эпик должен быть создан");
        assertEquals(epic.getId(), createdEpic.getId(), "Идентификаторы эпиков не должны различаться!");
        assertEquals(epic.getName(), createdEpic.getName(), "Имена эпиков не должны различаться.");
        assertEquals(epic.getTaskStatus(), createdEpic.getTaskStatus(), "Статусы эпиков не должны различаться.");
    }

    @Test
    void shouldUpdateEpic() {
        Epic createdEpic = taskManager.createEpic(epic);
        epic.setName("Обновленное имя эпика");
        taskManager.updateEpic(epic);
        Epic updatedEpic = taskManager.getEpic(createdEpic.getId());
        assertEquals(epic.getName(), updatedEpic.getName(), "Имя эпика должно быть обновлено");
    }

    @Test
    void shouldDeleteEpic() {
        Epic createdEpic = taskManager.createEpic(epic);
        taskManager.deleteEpic(createdEpic.getId());
        assertThrows(NotFoundException.class,
                () -> taskManager.getEpic(createdEpic.getId()),
                "Должно выбрасываться исключение при работе с несуществующим эпиком");
    }

    @Test
    void shouldCreateSubTask() {
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask createdSubTask = taskManager.createSubTask(subTask, createdEpic.getId());
        assertNotNull(createdSubTask, "Подзадача должна быть создана");
        assertEquals(subTask.getId(), createdSubTask.getId(), "Идентификаторы подзадач не должны различаться!");
        assertEquals(subTask.getEpicId(), createdSubTask.getEpicId(), "ID эпика подзадачи должен совпадать");
    }

    @Test
    void shouldUpdateSubTask() {
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask createdSubTask = taskManager.createSubTask(subTask, createdEpic.getId());
        subTask.setName("Обновленное имя подзадачи");
        taskManager.updateSubTask(subTask);
        SubTask updatedSubTask = taskManager.getSubTask(createdSubTask.getId());
        assertEquals(subTask.getName(), updatedSubTask.getName(), "Имя подзадачи должно быть обновлено");
    }

    @Test
    void shouldDeleteSubTask() {
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask createdSubTask = taskManager.createSubTask(subTask, createdEpic.getId());
        taskManager.deleteSubTask(createdSubTask.getId());
        assertThrows(NotFoundException.class,
                () -> taskManager.getSubTask(createdSubTask.getId()),
                "Должно выбрасываться исключение при работе с несуществующей подзадачей");
    }

    @Test
    void shouldGetEpicAllSubTasks() {
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask createdSubTask = taskManager.createSubTask(subTask, createdEpic.getId());
        List<SubTask> subTasks = taskManager.getEpicAllSubTasks(createdEpic.getId());
        assertEquals(1, subTasks.size(), "Должна быть 1 подзадача");
        assertEquals(createdSubTask, subTasks.get(0), "Подзадачи должны совпадать");
    }

    @Test
    void shouldCalculateEpicStatusWhenAllSubtasksNew() {
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask subTask1 = taskManager.createSubTask(
                new SubTask("Sub1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(10),
                        LocalDateTime.now().plusMinutes(10)), createdEpic.getId());
        SubTask subTask2 = taskManager.createSubTask(
                new SubTask("Sub2", "Desc2", TaskStatus.NEW, Duration.ofMinutes(15),
                LocalDateTime.now().plusMinutes(20)),
                createdEpic.getId());

        assertEquals(TaskStatus.NEW, createdEpic.getTaskStatus(),
                "Статус эпика должен быть NEW, когда все подзадачи NEW");
    }

    @Test
    void shouldCalculateEpicStatusWhenAllSubtasksDone() {
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask subTask1 = taskManager.createSubTask(
                new SubTask("SubTask1", "Desc1", TaskStatus.DONE, Duration.ofMinutes(10),
                        LocalDateTime.now()), createdEpic.getId());
        SubTask subTask2 = taskManager.createSubTask(
                new SubTask("SubTask2", "Desc2", TaskStatus.DONE, Duration.ofMinutes(15),
                        LocalDateTime.now().plusMinutes(10)), createdEpic.getId());

        assertEquals(TaskStatus.DONE, createdEpic.getTaskStatus(),
                "Статус эпика должен быть DONE, когда все подзадачи DONE");
    }

    @Test
    void shouldCalculateEpicStatusWhenSubtasksNewAndDone() {
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask subTask1 = taskManager.createSubTask(
                new SubTask("SubTask1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(10),
                        LocalDateTime.now().minusMinutes(10)), createdEpic.getId());
        SubTask subTask2 = taskManager.createSubTask(
                new SubTask("SubTask2", "Desc2", TaskStatus.DONE, Duration.ofMinutes(15),
                LocalDateTime.now()), createdEpic.getId());

        assertEquals(TaskStatus.IN_PROGRESS, createdEpic.getTaskStatus(),
                "Статус эпика должен быть IN_PROGRESS, когда подзадачи NEW и DONE");
    }

    @Test
    void shouldCalculateEpicStatusWhenSubtasksInProgress() {
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask subTask1 = taskManager.createSubTask(
                new SubTask("SubTask1", "Desc1", TaskStatus.IN_PROGRESS, Duration.ofMinutes(10),
                LocalDateTime.now()), createdEpic.getId());
        SubTask subTask2 = taskManager.createSubTask(
                new SubTask("SubTask1", "Desc2", TaskStatus.IN_PROGRESS, Duration.ofMinutes(15),
                        LocalDateTime.now().plusMinutes(10)), createdEpic.getId());

        assertEquals(TaskStatus.IN_PROGRESS, createdEpic.getTaskStatus(),
                "Статус эпика должен быть IN_PROGRESS, когда подзадачи IN_PROGRESS");
    }

    @Test
    void shouldCheckTaskTimeIntersection() {
        Task task1 = taskManager.createTask(
                new Task("Task1", "Desc1", TaskStatus.NEW,
                        Duration.ofMinutes(30),
                        LocalDateTime.of(2025, 1, 1, 10, 0)));
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 1, 1, 10, 15));

        assertTrue(taskManager.checkConfluence(task2),
                "Задачи должны пересекаться по времени");
    }

    @Test
    void shouldNotAllowTaskWithSameTime() {
        Task task1 = taskManager.createTask(
                new Task("Task1", "Desc1", TaskStatus.NEW,
                        Duration.ofMinutes(30),
                        LocalDateTime.of(2025, 1, 1, 10, 0)));
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 10, 0));

        assertTrue(taskManager.checkConfluence(task2),
                "Задачи с одинаковым временем должны считаться пересекающимися");
    }
}
