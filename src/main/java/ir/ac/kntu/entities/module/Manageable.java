package ir.ac.kntu.entities.module;

public interface Manageable {
    boolean isActive();

    void setActive(boolean active);

    String getSummary();
}
