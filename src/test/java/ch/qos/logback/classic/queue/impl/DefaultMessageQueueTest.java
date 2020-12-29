package ch.qos.logback.classic.queue.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMessageQueueTest {

    @Test
    void getInstance() {
        DefaultMessageQueue messageQueue1 = DefaultMessageQueue.getInstance();
    }

    @Test
    void getQueueForClass() {
    }
}