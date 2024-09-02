package model;

public class SubTask extends Task {
    int epicId;

    public SubTask(String name, String description, TaskStatus taskStatus) {
        super(name, description, taskStatus);
    }

    //  @Override
    public int getEpic() {
        return epicId;
    }

    public void setEpic(int epicId) {
        this.epicId = epicId;
    }
}
