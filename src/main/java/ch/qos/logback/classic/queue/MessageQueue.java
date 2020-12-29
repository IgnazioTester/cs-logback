package ch.qos.logback.classic.queue;

import ch.qos.logback.classic.model.LogEntry;

import java.util.Queue;

public interface MessageQueue {
    <T extends LogEntry> Queue<T> getQueueForClass(Class<T> c);
}
