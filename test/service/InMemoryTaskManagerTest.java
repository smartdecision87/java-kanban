package service;

import java.util.List;
import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    HistoryManager historyManager;
    TaskManager taskManager;
    Task task;
    Epic epic;
    SubTask subTask;

    @BeforeEach
    void initManagersAndTasks(){
        historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager(historyManager);
        task = new Task("A new task", "To do the new task", TaskStatus.NEW);
        epic = new Epic( "FOOD BUYING",
                "To buy food in a supermarket"
        );
        subTask = new SubTask("A call taxi",
                "A call taxi for getting to supermarket",
                TaskStatus.NEW
        );

    }

    @Test
    void shouldCreateTask() {
        Task newTask = taskManager.createTask(task);
        assertEquals(task.getId(), newTask.getId(), "Идентификаторы задач не должны различаться!");
        assertEquals(task.getName(), newTask.getName(), "Имена задач не должны различаться.");
        assertEquals(task.getTaskStatus(), newTask.getTaskStatus(), "Статусы задач не должны различатсья.");
    }

    @Test
    void shouldUpdateTask() {
        taskManager.createTask(task);
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        task.setName("Другое имя");
        taskManager.updateTask(task);
        Task newTask = taskManager.getTask(task.getId());
        assertEquals(task.getId(), newTask.getId(), "Идентификаторы задач не должны различаться!");
        assertEquals(task.getName(), newTask.getName(), "Имена задач не должны различаться.");
        assertEquals(task.getTaskStatus(), newTask.getTaskStatus(), "Статусы задач не должны различатсья.");
    }

    @Test
    void shouldGetTask() {
        taskManager.createTask(task);
        Task newTask = taskManager.getTask(task.getId());
        assertEquals(task.getId(), newTask.getId(), "Идентификаторы задач не должны различаться!");
        assertEquals(task.getName(), newTask.getName(), "Имена задач не должны различаться.");
        assertEquals(task.getTaskStatus(), newTask.getTaskStatus(), "Статусы задач не должны различатсья.");
    }

    @Test
    void shouldDeleteTask() {
        Task newTask = taskManager.createTask(task);
        taskManager.deleteTask(task.getId());
        newTask = taskManager.getTask(task.getId());
        assertNull(newTask, "Задача не была удалена из трекера!");
    }

    @Test
    void shouldGetAllTasks() {
        final Task task2 = new Task("A new task2", "To do the new task2", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.createTask(task2);
        final List<Task> tasksFromTaskManager = taskManager.getAllTasks();
        assertEquals(2, tasksFromTaskManager.size(), "Кол-во задач должно быть равно 2.");
    }

    @Test
    void shouldDeleteAllTasks() {
        final Task task2 = new Task("A new task2", "To do the new task2", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.createTask(task2);
        taskManager.deleteAllTasks();
        final List<Task> tasksFromTaskManager = taskManager.getAllTasks();
        assertEquals(0, tasksFromTaskManager.size(), "Трекер задач должен быть пустым.");
        assertTrue(tasksFromTaskManager.isEmpty(), "Трекер задач должен быть пустым.");
    }

    @Test
    void shouldCreateEpic() {
        Epic epicFromTaskManager = taskManager.createEpic(epic);
        assertEquals(epic.getId(), epicFromTaskManager.getId(), "Идентификаиторы задач не должны различаться!");
        assertEquals(epic.getName(), epicFromTaskManager.getName(), "Имена задач не должны различаться.");
        assertEquals(epic.getTaskStatus(), epicFromTaskManager.getTaskStatus(), "Статусы задач " +
                "не должны различатсья.");
        assertArrayEquals(epic.getAllSubTaskIds().toArray(), epicFromTaskManager.getAllSubTaskIds().toArray(),
                "Порядок и кол-во подзадач должно сохраняться.");
    }

    @Test
    void shouldUpdateEpic() {
        taskManager.createEpic(epic);
        epic.setName("Другое имя");
        taskManager.updateEpic(epic);
        Epic epicFromTaskManager = taskManager.getEpic(epic.getId());
        assertEquals(epic.getId(), epicFromTaskManager.getId(), "Идентификаиторы задач не должны различаться!");
        assertEquals(epic.getName(), epicFromTaskManager.getName(), "Имена задач не должны различаться.");
        assertEquals(epic.getTaskStatus(), epicFromTaskManager.getTaskStatus(), "Статусы задач " +
                "не должны различатсья.");
        assertArrayEquals(epic.getAllSubTaskIds().toArray(), epicFromTaskManager.getAllSubTaskIds().toArray(),
                "Порядок и кол-во подзадач должны сохраняться.");
    }

    @Test
    void shouldDeleteEpic() {
        taskManager.createEpic(epic);
        assertNotNull(taskManager.getEpic(epic.getId()), "Эпик должен присутствовать в трекере.");
        taskManager.deleteEpic(epic.getId());
        assertNull(taskManager.getEpic(epic.getId()), "Эпик должен отсутствовать в трекере.");
    }


    @Test
    void shouldGetAllEpics() {
        taskManager.createEpic(epic);
        assertEquals(1, taskManager.getAllEpics().size(), "В трекере должен быть 1 Эпик.");
    }

    @Test
    void shouldDeleteAllEpics() {
        taskManager.createEpic(epic);
        Epic epic2 = new Epic( "CAR BUYING",
                "To buy an used car"
        );
        taskManager.createEpic(epic2);
        assertEquals(2, taskManager.getAllEpics().size(), "Кол-во эпиков в трекере должно быть " +
                "равно 2");
    }

    @Test
    void shouldGetEpicAllSubTasks() {
        epic.addSubTask(subTask.getId());
        taskManager.createEpic(epic);

    }

    @Test
    void shouldCreateSubTask() {
        taskManager.createEpic(epic);
        SubTask subTaskFromTaskManager = taskManager.createSubTask(subTask, epic.getId());
        assertEquals(subTask.getId(), subTaskFromTaskManager.getId(), "Идентификаторы подзадач " +
                "не должны различаться!");
        assertEquals(subTask.getName(), subTaskFromTaskManager.getName(),
                "Имена подзадач не должны различаться.");
        assertEquals(subTask.getTaskStatus(), subTaskFromTaskManager.getTaskStatus(),
                "Статусы подзадач не должны различатсья.");
        assertEquals(subTask.getEpicId(), subTaskFromTaskManager.getEpicId(),
                "Идентификаторы эпика подзадач не должны различаться.");
    }

    @Test
    void shouldUpdateSubTask() {
        taskManager.createEpic(epic);
        SubTask subTaskFromTaskManager = taskManager.createSubTask(subTask, epic.getId());
        assertEquals(subTask.getId(), subTaskFromTaskManager.getId(), "Идентификаторы подзадач " +
                "не должны различаться!");
        assertEquals(subTask.getName(), subTaskFromTaskManager.getName(),
                "Имена подзадач не должны различаться.");
        assertEquals(subTask.getTaskStatus(), subTaskFromTaskManager.getTaskStatus(),
                "Статусы подзадач не должны различатсья.");
        assertEquals(subTask.getEpicId(), subTaskFromTaskManager.getEpicId(),
                "Идентификаторы эпика подзадач не должны различаться.");

    }

    @Test
    void shouldDeleteSubTask() {
        taskManager.createEpic(epic);
        taskManager.createSubTask(subTask, epic.getId());
        taskManager.deleteSubTask(subTask.getId());
        assertNull(taskManager.getSubTask(subTask.getId()), "Подзадачи не должно быть в трекере задач.");
    }
}