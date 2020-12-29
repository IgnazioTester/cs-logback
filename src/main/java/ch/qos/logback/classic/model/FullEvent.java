package ch.qos.logback.classic.model;

import java.util.Objects;

public class FullEvent implements LogEntry {
    private String id;
    private long startTimestamp;
    private long endTimestamp;
    private long duration;
    private String type;
    private String host;
    private boolean alert;

    public FullEvent(String id) {
        this.id = id;
        this.type = "";
        this.host = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
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

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullEvent fullEvent = (FullEvent) o;
        return startTimestamp == fullEvent.startTimestamp && endTimestamp == fullEvent.endTimestamp && duration == fullEvent.duration && alert == fullEvent.alert && id.equals(fullEvent.id) && type.equals(fullEvent.type) && host.equals(fullEvent.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
