package ch.qos.logback.classic.model;

public class StringEvent implements LogEntry {
    private String event;

    public StringEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
