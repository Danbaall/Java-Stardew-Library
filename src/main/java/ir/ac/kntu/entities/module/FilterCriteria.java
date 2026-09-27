package ir.ac.kntu.entities.module;

public interface FilterCriteria<T> {
    boolean matches(T candidate);
}
