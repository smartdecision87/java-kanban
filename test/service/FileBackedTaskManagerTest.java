package service;

import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager taskManager;

    @BeforeEach
    void init() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        taskManager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void clear() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void shouldLoadTasksFromFile() throws IOException {
        // Создаем задачи для сохранения
        Task task = new Task("New Task", "Task Description", TaskStatus.NEW);
        Task task2 = new Task("New Task2", "Task2 Description", TaskStatus.IN_PROGRESS);
        Task task3 = new Task("New Task3", "Task3 Description", TaskStatus.DONE);
        Epic epic = new Epic("New Epic", "Epic Description");
        SubTask subTask = new SubTask("New SubTask", "SubTask Description", TaskStatus.IN_PROGRESS);
        SubTask subTask2 = new SubTask("New SubTask2", "SubTask2 Description", TaskStatus.NEW);

        // Сохраняем задачи
        Task createdTask = taskManager.createTask(task);
        Task createdTask2 = taskManager.createTask(task2);
        Task createdTask3 = taskManager.createTask(task3);
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask createdSubTask = taskManager.createSubTask(subTask, createdEpic.getId());
        SubTask createdSubTask2 = taskManager.createSubTask(subTask2, createdEpic.getId());

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем, что задачи загрузились корректно
        Task loadedTask = loadedManager.getTask(createdTask.getId());
        Task loadedTask2 = loadedManager.getTask(createdTask2.getId());
        Task loadedTask3 = loadedManager.getTask(createdTask3.getId());
        Epic loadedEpic = loadedManager.getEpic(createdEpic.getId());
        SubTask loadedSubTask = loadedManager.getSubTask(createdSubTask.getId());
        SubTask loadedSubTask2 = loadedManager.getSubTask(createdSubTask2.getId());

        assertNotNull(loadedTask);
        assertNotNull(loadedTask2);
        assertNotNull(loadedTask3);
        assertNotNull(loadedEpic);
        assertNotNull(loadedSubTask);
        assertNotNull(loadedSubTask2);

        // проверка задачи 1
        assertEquals(createdTask.getId(), loadedTask.getId(), "Идентификаторы задач не должны различаться!");
        assertEquals(createdTask.getName(), loadedTask.getName(), "Имена задач не должны различаться.");
        assertEquals(createdTask.getTaskStatus(), loadedTask.getTaskStatus(),
                "Статусы задач не должны различатсья.");

        // проверка задачи 2
        assertEquals(createdTask2.getId(), loadedTask2.getId(), "Идентификаторы задач не должны различаться!");
        assertEquals(createdTask2.getName(), loadedTask2.getName(), "Имена задач не должны различаться.");
        assertEquals(createdTask2.getTaskStatus(), loadedTask2.getTaskStatus(),
                "Статусы задач не должны различатсья.");

        // проверка задачи 3
        assertEquals(createdTask3.getId(), loadedTask3.getId(), "Идентификаторы задач не должны различаться!");
        assertEquals(createdTask3.getName(), loadedTask3.getName(), "Имена задач не должны различаться.");
        assertEquals(createdTask3.getTaskStatus(), loadedTask3.getTaskStatus(),
                "Статусы задач не должны различатсья.");

        // проверка эпика
        assertEquals(createdEpic.getId(), loadedEpic.getId(), "Идентификаторы эпиков не должны различаться!");
        assertEquals(createdEpic.getName(), loadedEpic.getName(), "Имена эпиков не должны различаться.");
        assertEquals(createdEpic.getTaskStatus(), loadedEpic.getTaskStatus(),
                "Статусы эпиков не должны различатсья.");
        assertArrayEquals(createdEpic.getAllSubTaskIds().toArray(), loadedEpic.getAllSubTaskIds().toArray(),
                "Подзадачи эпиков не должны различаться");

        // проверка подзадачи 1
        assertEquals(createdSubTask.getId(), loadedSubTask.getId(),
                "Идентификаторы задач не должны различаться!");
        assertEquals(createdSubTask.getName(), loadedSubTask.getName(), "Имена задач не должны различаться.");
        assertEquals(createdSubTask.getTaskStatus(), loadedSubTask.getTaskStatus(),
                "Статусы задач не должны различатсья.");
        assertEquals(createdSubTask.getEpicId(), loadedSubTask.getEpicId(),
                "Идентификаторы эпиков для подзадач не должны различаться!");

        // проверка подзадачи 2
        assertEquals(createdSubTask2.getId(), loadedSubTask2.getId(),
                "Идентификаторы задач не должны различаться!");
        assertEquals(createdSubTask2.getName(), loadedSubTask2.getName(), "Имена задач не должны различаться.");
        assertEquals(createdSubTask2.getTaskStatus(), loadedSubTask2.getTaskStatus(),
                "Статусы задач не должны различатсья.");
        assertEquals(createdSubTask2.getEpicId(), loadedSubTask2.getEpicId(),
                "Идентификаторы эпиков для подзадач не должны различаться!");
    }

    @Test
    void shouldSaveAndLoadFromEmptyFile() throws IOException {
        // Сохраняем пустой менеджер
        taskManager.save();

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем, что задачи не загрузились
        assertTrue(loadedManager.getAllTasks().isEmpty(), "Не должно быть задач!");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Не должно быть эпиков!");
        assertTrue(loadedManager.getAllSubTasks().isEmpty(), "Не должно быть подзадач!");
    }
}