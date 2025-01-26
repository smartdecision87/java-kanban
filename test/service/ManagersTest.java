package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void shouldGetDefaultManager() {
        final TaskManager inMemoryTaskManager = Managers.getDefault();
        assertNotNull(inMemoryTaskManager, "Менеджер должен быть проиницализирован.");
    }
}