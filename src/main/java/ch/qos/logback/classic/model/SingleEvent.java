package ch.qos.logback.classic.model;

import ch.qos.logback.classic.enums.StateEnum;

public class SingleEvent implements LogEntry {
    private String id;
    private StateEnum state;
    private long timestamp;
    private String type;
    private String host;

    public SingleEvent(String id, StateEnum state, long timestamp) {
        this.id = id;
        this.state = state;
        this.timestamp = timestamp;
        this.type = "";
        this.host = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StateEnum getState() {
        return state;
    }

    public void setState(StateEnum state) {
        this.state = state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
