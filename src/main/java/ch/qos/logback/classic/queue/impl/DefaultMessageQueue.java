package ch.qos.logback.classic.queue.impl;

import ch.qos.logback.classic.model.LogEntry;
import ch.qos.logback.classic.queue.MessageQueue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class DefaultMessageQueue<T extends LogEntry> implements MessageQueue {
    private final Map<String, Queue<T>> queues = new HashMap<>();
    private static DefaultMessageQueue instance = null;

    private DefaultMessageQueue(){}

    public static DefaultMessageQueue getInstance() {
        if (instance == null)
            instance = new DefaultMessageQueue<>();

        return instance;
    }

    public Queue<T> getQueueForClass(Class<T> c) {
        if (!queues.containsKey(c.getName()))
            queues.put(c.getName(), new LinkedList<>());

        return queues.get(c.getName());
    }
}
