package service;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import exception.NotFoundException;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    protected TaskManager manager;
    protected HttpServer httpServer;
    protected HttpTaskServer httpTaskServer;
    protected Gson gson;
    protected HttpClient client;
    protected Task task, task2;
    protected SubTask subTask, subTask2, subTask3;
    protected Epic epic;

    @BeforeEach
    public void init() throws IOException {
        httpTaskServer = new HttpTaskServer();
        manager = httpTaskServer.getTaskManager();
        httpTaskServer.initialize();
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        httpTaskServer.startServer();

        task = new Task("Erase",
                "To erase data in Database",
                TaskStatus.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.now());
        manager.createTask(task);

        task2 = new Task("Add",
                "To add data in Database",
                TaskStatus.NEW,
                Duration.ofMinutes(15),
                LocalDateTime.now().plusMinutes(10));
        manager.createTask(task2);

        epic = new Epic("FOOD BUYING",
                "To buy food in a supermarket"
        );
        manager.createEpic(epic);

        subTask = new SubTask("A call taxi",
                "A call taxi for getting to supermarket",
                TaskStatus.NEW,
                Duration.ofMinutes(7),
                LocalDateTime.now().plusMinutes(25)
        );
        manager.createSubTask(subTask, epic.getId());

        final Epic epic2 = new Epic("CAR BUYING",
                "To buy a car"
        );
        manager.createEpic(epic2);

        subTask2 = new SubTask("A call taxi",
                "A call taxi for getting to supermarket",
                TaskStatus.NEW,
                Duration.ofMinutes(12),
                LocalDateTime.now().plusMinutes(32)
        );
        manager.createSubTask(subTask2, epic2.getId());

        subTask3 = new SubTask("A call taxi",
                "A call taxi for getting to supermarket",
                TaskStatus.NEW,
                Duration.ofMinutes(5),
                LocalDateTime.now().plusMinutes(44)
        );
        manager.createSubTask(subTask3, epic2.getId());
    }

    @AfterEach
    public void shutDown() {
        httpTaskServer.stopServer();
    }

    protected Task createTestTask() {
        return new Task("Test Task", "Test Description",
                TaskStatus.NEW, Duration.ofMinutes(30),
                LocalDateTime.now().plusHours(1));
    }

    @Test
    void shouldGetAllTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks);
        assertEquals(2, tasks.length);
    }

    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(responseTask);
        assertEquals(task.getId(), responseTask.getId());
        assertEquals(task.getName(), responseTask.getName());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentTask() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description",
                TaskStatus.NEW, Duration.ofMinutes(30),
                LocalDateTime.now().plusHours(1));
        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8082/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();
        assertEquals(3, tasksFromManager.size());
        assertEquals("Test Task", tasksFromManager.get(2).getName());
    }

    @Test
    void shouldUpdateTask() throws IOException, InterruptedException {
        Task task = manager.createTask(createTestTask());
        task.setName("Updated Task");
        task.setTaskStatus(TaskStatus.DONE);

        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8082/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task updatedTask = manager.getTask(task.getId());
        assertEquals("Updated Task", updatedTask.getName());
        assertEquals(TaskStatus.DONE, updatedTask.getTaskStatus());
    }

    @Test
    void shouldDeleteTask() throws IOException, InterruptedException {
        Task task = manager.createTask(createTestTask());

        URI url = URI.create("http://localhost:8082/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class,
                () -> manager.getTask(epic.getId()),
                "Должно выбрасываться исключение при работе с несуществующим эпиком");
    }

    @Test
    void shouldReturnConflictWhenCreatingTaskWithTimeOverlap() throws IOException, InterruptedException {
        Task task3 = new Task("Task 1", "Desc 1",
                TaskStatus.NEW, Duration.ofMinutes(60),
                LocalDateTime.of(2025, 1, 1, 10, 0));
        manager.createTask(task3);

        Task task4 = new Task("Task 2", "Desc 2",
                TaskStatus.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 10, 30));

        String taskJson = gson.toJson(task4);

        URI url = URI.create("http://localhost:8082/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    // Tests for /epics endpoint
    @Test
    void shouldGetAllEpics() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertNotNull(epics);
        assertEquals(2, epics.length);
    }

    @Test
    void shouldGetEpicById() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic responseEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(responseEpic);
        assertEquals(epic.getId(), responseEpic.getId());
        assertEquals(epic.getName(), responseEpic.getName());
    }

    @Test
    void shouldGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Test Epic", "Test Description"));
        SubTask subTask = manager.createSubTask(
                new SubTask("SubTask 1", "Desc 1", TaskStatus.NEW,
                        Duration.ofMinutes(30), LocalDateTime.now().plusHours(3)),
                epic.getId()
        );

        URI url = URI.create("http://localhost:8082/epics/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        SubTask[] subTasks = gson.fromJson(response.body(), SubTask[].class);
        assertNotNull(subTasks);
        assertEquals(1, subTasks.length);
        assertEquals(subTask.getId(), subTasks[0].getId());
    }

    @Test
    void shouldCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("New Epic", "Epic Description");
        String epicJson = gson.toJson(epic);

        URI url = URI.create("http://localhost:8082/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getAllEpics();
        assertEquals(3, epicsFromManager.size());
        assertEquals("New Epic", epicsFromManager.get(2).getName());
    }

    @Test
    void shouldDeleteEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Test Epic", "Test Description"));

        URI url = URI.create("http://localhost:8082/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class,
                () -> manager.getEpic(epic.getId()),
                "Должно выбрасываться исключение при работе с несуществующим эпиком");
    }

    // Tests for /subtasks endpoint
    @Test
    void shouldGetAllSubtasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        SubTask[] subTasks = gson.fromJson(response.body(), SubTask[].class);
        assertNotNull(subTasks);
        assertEquals(3, subTasks.length);
        assertArrayEquals(manager.getAllSubTasks().toArray(), Arrays.stream(subTasks).toArray());
    }

    @Test
    void shouldGetSubtaskById() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/subtasks/" + subTask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        SubTask responseSubTask = gson.fromJson(response.body(), SubTask.class);
        assertNotNull(responseSubTask);
        assertEquals(subTask.getId(), responseSubTask.getId());
        assertEquals(subTask.getName(), responseSubTask.getName());
    }

    @Test
    void shouldCreateSubtask() throws IOException, InterruptedException {
        SubTask subTask4 = new SubTask("New SubTask", "Description",
                TaskStatus.NEW, Duration.ofMinutes(30),
                LocalDateTime.now().plusHours(1));
        subTask4.setEpicId(epic.getId());

        String subTaskJson = gson.toJson(subTask4);

        URI url = URI.create("http://localhost:8082/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный код возврата");

        List<SubTask> subTasksFromManager = manager.getAllSubTasks();

        assertNotNull(subTasksFromManager, "Задачи не возвращаются");
        assertEquals(4, subTasksFromManager.size(), "Некорректное количество задач.");
        assertEquals("New SubTask", subTasksFromManager.get(3).getName(), "Некорректное имя задачи");
    }

    @Test
    void shouldUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Test Epic", "Test Description"));
        SubTask subTask = manager.createSubTask(
                new SubTask("Test SubTask", "Test Description", TaskStatus.NEW,
                        Duration.ofMinutes(30), LocalDateTime.now().plusHours(1)),
                epic.getId()
        );

        subTask.setName("Updated SubTask");
        subTask.setTaskStatus(TaskStatus.DONE);

        String subTaskJson = gson.toJson(subTask);

        URI url = URI.create("http://localhost:8082/subtasks/" + subTask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        SubTask updatedSubTask = manager.getSubTask(subTask.getId());
        assertEquals("Updated SubTask", updatedSubTask.getName(), "Некорректное имя задачи.");
        assertEquals(TaskStatus.DONE, updatedSubTask.getTaskStatus(), "Некорректный статус задачи.");
    }

    @Test
    void shouldDeleteSubtask() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/subtasks/" + subTask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class,
                () -> manager.getSubTask(subTask.getId()),
                "Должно выбрасываться исключение при работе с несуществующей подзадачей");
    }

    // Tests for /history and /prioritized endpoints
    @Test
    void shouldGetHistory() throws IOException, InterruptedException {
        manager.getTask(task.getId());
        manager.getSubTask(subTask.getId());

        URI url = URI.create("http://localhost:8082/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertNotNull(history, "История задач не возвращается.");
        assertEquals(2, history.length, "Некорректный размер истории.");
    }

    @Test
    void shouldGetPrioritizedTasks() throws IOException, InterruptedException {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubTasks();

        task = manager.createTask(new Task("Task 1", "Desc 1",
                TaskStatus.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 10, 0)));

        task2 = manager.createTask(new Task("Task 1", "Desc 1",
                TaskStatus.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 9, 0)));

        epic = manager.createEpic(new Epic("Test Epic", "Test Description"));
        subTask = manager.createSubTask(
                new SubTask("SubTask", "Desc", TaskStatus.NEW,
                        Duration.ofMinutes(20),
                        LocalDateTime.of(2025, 1, 1, 11, 0)),
                epic.getId()
        );

        URI url = URI.create("http://localhost:8082/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertNotNull(prioritized);
        assertEquals(3, prioritized.length);

        assertEquals(task2.getId(), prioritized[0].getId());
        assertEquals(task.getId(), prioritized[1].getId());
        assertEquals(subTask.getId(), prioritized[2].getId());
    }

    @Test
    void shouldReturnEmptyHistoryWhenNoTasksViewed() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertNotNull(history);
        assertEquals(0, history.length);
    }

    // Edge case tests
    @Test
    void shouldReturnNotFoundForInvalidEndpoint() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/foo");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnBadRequestForInvalidTaskId() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/tasks/abc");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentEpic() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/epics/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentSubtask() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8082/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}