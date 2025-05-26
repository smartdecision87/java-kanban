package service;


import model.Task;
import model.Node;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

      private final Map<Integer, Node> taskHistory;
      private Node firstNode;
      private Node currentNode;

    public InMemoryHistoryManager() {
        taskHistory = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (taskHistory.containsKey(task.getId())) {
            removeNode(taskHistory.get(task.getId()));
        }
        taskHistory.put(task.getId(), linkLast(task));
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        Node node = taskHistory.get(id);
        removeNode(taskHistory.get(id));
        taskHistory.remove(id);
    }

    private Node linkLast(Task task) {
        Node node = new Node(task);

        if (currentNode != null) {
            currentNode.nextNode = node;
            node.prevNode = currentNode;
        } else {
            firstNode = node;
        }
        currentNode = node;

        return node;
    }

    private List<Task> getTasks() {
        List<Task> taskList = new ArrayList<>();
        Node node = firstNode;

        while (node != null) {
            taskList.add(node.task);
            node = node.nextNode;
        }

        return taskList;
    }

    private void removeNode(Node node) {
        if (node.prevNode != null) {
            node.prevNode.nextNode = node.nextNode;
        } else {
            firstNode = node.nextNode;
        }
        if (node.nextNode != null) {
            node.nextNode.prevNode = node.prevNode;
        } else {
            currentNode = node.prevNode;
        }
    }
}