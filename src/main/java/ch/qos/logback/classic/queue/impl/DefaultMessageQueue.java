package ch.qos.logback.classic.queue.impl;

import ch.qos.logback.classic.model.LogEntry;
import ch.qos.logback.classic.queue.MessageQueue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class DefaultMessageQueue implements MessageQueue {
    private final  Map<String, Queue<?>> queues = new HashMap<>();
    private static DefaultMessageQueue instance = null;

    private DefaultMessageQueue(){}

    public static DefaultMessageQueue getInstance() {
        if (instance == null)
            instance = new DefaultMessageQueue();

        return instance;
    }

    public <T extends LogEntry>  Queue<T> getQueueForClass(Class<T> c) {
        if (!queues.containsKey(c.getName()))
            queues.put(c.getName(), new LinkedList<>());

        return (Queue<T>) queues.get(c.getName());
    }
}
