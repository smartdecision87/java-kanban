package model;

public class Node {
    // public int id;
    public Node prevNode;
    public Node nextNode;
    public Task task;

    public Node(Task task) {
        this.task = task;
    }

}
