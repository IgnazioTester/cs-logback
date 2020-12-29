package ch.qos.logback.classic.data.store;

public interface RelationalDataStore<T> {
    void initDataStore();

    T getDataStore();

    void clearTables();
}
