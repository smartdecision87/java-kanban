package model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

class EpicTest {

    Epic epic;
    SubTask subTask1;
    SubTask subTask2;

    @BeforeEach
    void initEpic() {
       epic = new Epic( "FOOD BUYING", "To buy food in a supermarket");
       subTask1 = new SubTask("A call taxi","A call taxi for getting to supermarket", TaskStatus.NEW,
                                Duration.ofMinutes(3)
       );
       subTask2 = new SubTask("Carrot Buying","To buy a few carrots", TaskStatus.NEW,
                                Duration.ofMinutes(10)
       );
       subTask1.setId(1);
       subTask2.setId(2);
    }

    @Test
    void shouldGetAllSubTaskIds() {
        final ArrayList<Integer> array = new ArrayList<>();
        array.add(1);
        array.add(2);
        epic.addSubTask(subTask1.getId());
        assertEquals(1, epic.getAllSubTaskIds().toArray()[0], "Номер подзадачи не равен 1");
        epic.addSubTask(subTask2.getId());
        assertNotNull(epic.getAllSubTaskIds(), "Подзадачи не возвращаются.");
        assertArrayEquals(array.toArray(), epic.getAllSubTaskIds().toArray(),
                "Возвращен неверный список подазадач.");
    }

    @DisplayName("Добавляем два подзадачи в эпик, и удаляем одну из них")
    @Test
    void shouldDeleteSubTask() {
    /*    epic.addSubTask(subTask1.getId());
        epic.deleteSubTask(subTask1.getId());
    */
        epic.addSubTask(subTask1.getId());
        epic.addSubTask(subTask2.getId());
        assertEquals(2, epic.getAllSubTaskIds().size(),
                "Кол-во подзадач эпика не соответствует ожидаемому.");
        epic.deleteSubTask(subTask2.getId());
        assertEquals(1, epic.getAllSubTaskIds().size(),
                "Кол-во подзадач эпика не соответствует ожидаемому.");
    }

    @Test
    void shouldComputeEpicStatus() {
        TaskStatus taskStatus;
        final HashMap<Integer, SubTask> subTasks = new HashMap<>();

        subTasks.put(subTask1.getId(), subTask1);
        subTasks.put(subTask2.getId(), subTask2);
        epic.addSubTask(subTask2.getId());
        epic.addSubTask(subTask1.getId());
        taskStatus = epic.computeEpicStatus(subTasks);
        assertEquals(TaskStatus.NEW, taskStatus, "Статус эпика не соответствует состоянию IN_PROGRESS.");

        subTask1.setTaskStatus(TaskStatus.DONE);
        subTask2.setTaskStatus(TaskStatus.DONE);
        taskStatus = epic.computeEpicStatus(subTasks);
        assertEquals(TaskStatus.DONE, taskStatus, "Статус эпика не соответствует состоянию DONE.");
    }

    @Test
    void deleteAllSubTasks() {
        final ArrayList<Integer> array = new ArrayList<>();
        epic.addSubTask(subTask2.getId());
        epic.addSubTask(subTask1.getId());
        epic.deleteAllSubTasks();
        assertArrayEquals(array.toArray(), epic.getAllSubTaskIds().toArray(), "Не возвращает пустой массив.");
    }
}