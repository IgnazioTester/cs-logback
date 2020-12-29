package ch.qos.logback.classic.data.store.impl;

import ch.qos.logback.classic.data.store.RelationalDataStore;
import org.jdbi.v3.core.Jdbi;

import java.util.Properties;

public class DefaultJDBIDataStore implements RelationalDataStore<Jdbi> {
    private static final String CHECK_LOGBACK_TABLE_QUERY
            = "SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_name LIKE 'LOGBACK'";
    private static final String CREATE_LOGBACK_TABLE_QUERY
            = "CREATE TABLE LOGBACK (Id IDENTITY, Event_Id VARCHAR(100), Duration BIGINT, Type VARCHAR(100), Host VARCHAR(100), Alert BOOLEAN, PRIMARY KEY (Event_ID))";
    private static final String DELETE_ALL_FROM_LOGBACK = "DELETE FROM LOGBACK";

    private Jdbi dataStore;

    public DefaultJDBIDataStore() {
        // Empty constructor
    }

    @Override
    public void initDataStore() {
        if (dataStore != null)
            return;

        Properties properties = new Properties();
        properties.setProperty("username", "ignaziotester");
        properties.setProperty("password", "W3lc0m3");
        dataStore = Jdbi.create("jdbc:hsqldb:file:data/hsqldb", properties);

        dataStore.useHandle(handle -> {
            if (!handle.createQuery(CHECK_LOGBACK_TABLE_QUERY).mapToMap().findFirst().isPresent()) {
                handle.execute(CREATE_LOGBACK_TABLE_QUERY);
            }
        });
    }

    @Override
    public Jdbi getDataStore() {
        initDataStore();
        return dataStore;
    }

    @Override
    public void clearTables() {
        initDataStore();
        dataStore.useHandle(handle -> handle.execute(DELETE_ALL_FROM_LOGBACK));
    }
}
