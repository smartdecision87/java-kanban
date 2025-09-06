package service;

import com.google.gson.*;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.OverlapsException;
import model.Epic;
import model.SubTask;
import model.TaskStatus;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class HttpTaskServer {
    private static final int PORT = 8082;
    private static HttpServer httpServer;
    private static final TaskManager taskManager = Managers.getDefault();
    private static List<Task> tasks = new ArrayList<>();
    private static Gson gson;
    private static ErrorHandler errorHandler;

    public void initialize() throws IOException {
        if (httpServer == null) {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/tasks", new TaskHandler());
            httpServer.createContext("/subtasks", new SubtaskHandler());
            httpServer.createContext("/epics", new EpicHandler());
            httpServer.createContext("/history", new HistoryHandler());
            httpServer.createContext("/prioritized", new PrioritizedHandler());
            gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .create();
            errorHandler = new ErrorHandler();
        }
    }

    public static void main(String[] args) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.start();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        httpServer.createContext("/tasks", new TaskHandler());
        httpServer.createContext("/subtasks", new SubtaskHandler());
        httpServer.createContext("/epics", new EpicHandler());
        httpServer.createContext("/history", new HistoryHandler());
        httpServer.createContext("/prioritized", new PrioritizedHandler());
        errorHandler = new ErrorHandler();

        final Task task = new Task("Erase",
                "To erase data in Database",
                TaskStatus.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.now());
        taskManager.createTask(task);

        final Task task2 = new Task("Add",
                "To add data in Database",
                TaskStatus.NEW,
                Duration.ofMinutes(15),
                LocalDateTime.now().plusMinutes(10));
        taskManager.createTask(task2);
        
        final Epic epic = new Epic( "FOOD BUYING","To buy food in a supermarket");
        taskManager.createEpic(epic);

        final SubTask subTask = new SubTask("A call taxi",
                "A call taxi for getting to supermarket",
                TaskStatus.NEW,
                Duration.ofMinutes(7),
                LocalDateTime.now().plusMinutes(25)
        );
        taskManager.createSubTask(subTask, epic.getId());

        final Epic epic2 = new Epic( "CAR BUYING","To buy a car");
        taskManager.createEpic(epic2);

        final SubTask subTask2 = new SubTask("A call taxi",
                "A call taxi for getting to supermarket",
                TaskStatus.NEW,
                Duration.ofMinutes(12),
                LocalDateTime.now().plusMinutes(32)
        );
        taskManager.createSubTask(subTask2, epic2.getId());

        final SubTask subTask3 = new SubTask("A call taxi",
                "A call taxi for getting to supermarket",
                TaskStatus.NEW,
                Duration.ofMinutes(5),
                LocalDateTime.now().plusMinutes(44)
        );
        taskManager.createSubTask(subTask3, epic2.getId());
        //httpServer.stop(1);
    }


    public static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
            if (localDateTime == null) jsonWriter.nullValue();
            jsonWriter.value(localDateTime.format(formatter));
        }

        @Override
        public LocalDateTime read(JsonReader jsonReader) throws IOException {
            return LocalDateTime.parse(jsonReader.nextString(), formatter);
        }
    }

    public static class DurationAdapter extends TypeAdapter<Duration> {
        @Override
        public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
            jsonWriter.value(duration.toMinutes());
        }

        @Override
        public Duration read(JsonReader jsonReader) throws IOException {
            return Duration.ofMinutes(jsonReader.nextLong());
        }
    }

    public static Gson getGson() {
        return gson;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    static class TaskHandler extends BaseHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Началась обработка /tasks запроса от клиента.");
            String responseMethod = httpExchange.getRequestMethod();
            String uriPath = httpExchange.getRequestURI().getPath();
            String stringNumber = null;
            Task taskCreated;
            if (uriPath.split("/").length == 3) {
                stringNumber = uriPath.split("/")[2];
            }
            int id = -1;
            if (stringNumber != null) {
                Pattern pattern = Pattern.compile("^[1-9][0-9]*$");
                if (pattern.matcher(stringNumber).find()) {
                    id = Integer.parseInt(uriPath.split("/")[2]);
                } else {
                    sendInvalidQuery(httpExchange, "Некорректно задан идентификатор задачи!\n");
                    return;
                }
            }

            if ("GET".equals(responseMethod)) {
                try {
                    if (id == -1) {
                        tasks = taskManager.getAllTasks();
                        sendText(httpExchange, gson.toJson(tasks));
                    } else {
                        Task task = taskManager.getTask(id);
                        String data = gson.toJson(task);
                        sendText(httpExchange, data);
                    }
                } catch (Exception e) {
                    errorHandler.handle(httpExchange, e);
                }
            } else if ("POST".equals(responseMethod)) {
                // десериализация данных
                try {
                    InputStream inputStream = httpExchange.getRequestBody();
                    String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    Task taskFromClient = gson.fromJson(response, Task.class);
                    if (id == -1) {
                        taskCreated = taskManager.createTask(taskFromClient);
                        taskCreated = taskFromClient;
                        sendCreated(httpExchange, "Задача создана!\n");
                    } else {
                        taskManager.updateTask(taskFromClient);
                        sendCreated(httpExchange, "Задача обновлена!\n");
                    }
                } catch (Exception e) {
                    errorHandler.handle(httpExchange, e);
                }
            } else if ("DELETE".equals(responseMethod)) {
                try {
                    Task task = taskManager.getTask(id);
                    taskManager.deleteTask(id);
                    sendText(httpExchange, "Задача удалена!\n");
                } catch (Exception e) {
                    errorHandler.handle(httpExchange, e);
                }
            } else {
                sendInvalidQuery(httpExchange, "Неверный эндпойнт!\n");
            }
        }
    }

    static class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Началась обработка /subtasks запроса от клиента.");
            String responseMethod = httpExchange.getRequestMethod();
            String URIPath = httpExchange.getRequestURI().getPath();
            String stringNumber = null;
            SubTask subtaskCreated;
            if (URIPath.split("/").length == 3) {
                stringNumber = URIPath.split("/")[2];
            }
            int id = -1;
            if (stringNumber != null) {
                Pattern pattern = Pattern.compile("^[1-9][0-9]*$");
                if (pattern.matcher(stringNumber).find()) {
                    id = Integer.parseInt(URIPath.split("/")[2]);
                } else {
                    sendInvalidQuery(httpExchange, "Некорректно задан идентификатор подзадачи!\n");
                    return;
                }
            }

            if ("GET".equals(responseMethod)) {
                try {
                    if (id == -1) {
                        sendText(httpExchange, gson.toJson(taskManager.getAllSubTasks()));
                        return;
                    }
                    SubTask subtask = taskManager.getSubTask(id);
                    String data = gson.toJson(subtask);
                    sendText(httpExchange, data);
                } catch (Exception e) {
                    errorHandler.handle(httpExchange, e);
                }
            } else if ("POST".equals(responseMethod)) {
                // десериализация данных
                try {
                    InputStream inputStream = httpExchange.getRequestBody();
                    String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    SubTask subtaskFromClient = gson.fromJson(response, SubTask.class);
                    if (id == -1) {
                        subtaskCreated = taskManager.createSubTask(subtaskFromClient,
                                subtaskFromClient.getEpicId());
                        subtaskCreated = subtaskFromClient;
                        sendCreated(httpExchange, "Подзадача создана!\n");
                    } else {
                        taskManager.updateTask(subtaskFromClient);
                        sendCreated(httpExchange, "Подзадача обновлена!\n");
                    }
                } catch (Exception e) {
                    errorHandler.handle(httpExchange, e);
                }
            } else if ("DELETE".equals(responseMethod)) {
                try {
                    SubTask subtask = taskManager.getSubTask(id);
                    taskManager.deleteSubTask(id);
                    sendText(httpExchange, "Подзадача удалена!\n");
                } catch (Exception e) {
                    errorHandler.handle(httpExchange, e);
                }
            } else {
                sendInvalidQuery(httpExchange, "Неверный эндпойнт!\n");
            }
        }
    }

    static class EpicHandler extends BaseHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Началась обработка /epics запроса от клиента.");
            String responseMethod = httpExchange.getRequestMethod();
            String URIPath = httpExchange.getRequestURI().getPath();
            String stringNumber = null;
            String stringSubtasks = null;
            Epic epicCreated;
            if (URIPath.split("/").length >= 3) {
                stringNumber = URIPath.split("/")[2];
            }
            int id = -1;
            if (stringNumber != null) {
                Pattern pattern = Pattern.compile("^[1-9][0-9]*$");
                if (pattern.matcher(stringNumber).find()) {
                    id = Integer.parseInt(URIPath.split("/")[2]);
                } else {
                    sendInvalidQuery(httpExchange, "Некорректно задан идентификатор подзадачи!\n");
                    return;
                }
            }

            if ("GET".equals(responseMethod)) {
                try {
                    if (id == -1) {
                        List<Epic> epics = taskManager.getAllEpics();
                        sendText(httpExchange, gson.toJson(epics));
                        return;
                    }
                    Epic epic = taskManager.getEpic(id);
                    int splitURIPathLength = URIPath.split("/").length;
                    if (splitURIPathLength == 4) {
                        stringSubtasks = URIPath.split("/")[3];
                        if (!"subtasks".equals(stringSubtasks)) {
                            sendInvalidQuery(httpExchange, "Некорректно задан идентификатор эпика!\n");
                        } else {
                            sendText(httpExchange, gson.toJson(taskManager.getEpicAllSubTasks(id)));
                        }
                    } else if (splitURIPathLength == 3) {
                        sendText(httpExchange, gson.toJson(epic));
                    }
                } catch (Exception e) {
                    errorHandler.handle(httpExchange, e);
                }
            } else if ("POST".equals(responseMethod)) {
                // десериализация данных
                InputStream inputStream = httpExchange.getRequestBody();
                String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                Epic epicFromClient = gson.fromJson(response, Epic.class);
                epicCreated = taskManager.createEpic(epicFromClient);
                epicCreated = epicFromClient;
                sendCreated(httpExchange, "Эпик создан!\n");
            } else if ("DELETE".equals(responseMethod)) {
                try {
                    Epic epic = taskManager.getEpic(id);
                    taskManager.deleteEpic(id);
                    sendText(httpExchange, "Эпик удален!\n");
                } catch (Exception e) {
                    errorHandler.handle(httpExchange, e);
                }
            } else {
                sendInvalidQuery(httpExchange, "Неверный эндпойнт!\n");
            }
        }
    }

    static class HistoryHandler extends BaseHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Началась обработка /history запроса от клиента.");
            tasks = taskManager.getHistory();
            sendText(httpExchange, gson.toJson(tasks));
        }
    }

    static class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Началась обработка /prioritized запроса от клиента.");
            Set<Task> taskSet = taskManager.getPrioritizedTasks();
            sendText(httpExchange, gson.toJson(taskSet));
        }
    }

    public void startServer() {
        httpServer.start();
        System.out.println("Сервер запущен на порту " + PORT);
    }

    public void stopServer() {
        httpServer.stop(1);
        System.out.println("Сервер остановлен");
    }

    static class BaseHttpHandler {
        protected void sendText(HttpExchange httpExchange, String text) throws IOException {
            byte[] response = text.getBytes(StandardCharsets.UTF_8);
            httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            httpExchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response);
            }
        }

        protected void sendCreated(HttpExchange httpExchange, String text) throws IOException {
            byte[] response = text.getBytes(StandardCharsets.UTF_8);
            httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            httpExchange.sendResponseHeaders(201, response.length);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response);
            }
        }

        protected void sendInvalidQuery(HttpExchange httpExchange, String text) throws IOException {
            byte[] response = text.getBytes(StandardCharsets.UTF_8);
            httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            httpExchange.sendResponseHeaders(400, response.length);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

    static class ErrorHandler {
        public void handle(HttpExchange h, Exception e) throws IOException {
            int statusCode = 520;
            String text = "";
            try {
                if (e instanceof ManagerSaveException) {
                    statusCode = 500;
                }
                if (e instanceof OverlapsException) {
                    statusCode = 406;
                }
                if (e instanceof NotFoundException) {
                    statusCode = 404;
                }
                byte[] resp = e.getMessage().getBytes(StandardCharsets.UTF_8);
                h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
                h.sendResponseHeaders(statusCode, resp.length);
                h.getResponseBody().write(resp);
                h.close();
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }
}
