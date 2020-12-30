package ch.qos.logback.classic.service.impl;

import ch.qos.logback.classic.enums.StateEnum;
import ch.qos.logback.classic.model.FullEvent;
import ch.qos.logback.classic.model.SingleEvent;
import ch.qos.logback.classic.queue.impl.DefaultMessageQueue;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class DefaultEventMergerServiceTest {
    DefaultEventMergerService service = new DefaultEventMergerService();

    private Queue<SingleEvent> eventsQueue = DefaultMessageQueue.getInstance().getQueueForClass(SingleEvent.class);
    private Queue<FullEvent> fullEventsQueue = DefaultMessageQueue.getInstance().getQueueForClass(FullEvent.class);

    @Test
    void mergeEvents_ValidData_ShouldPass() {
        List<SingleEvent> events = new LinkedList<>();

        events.add(new SingleEvent("scsmbstgra", StateEnum.valueOf("STARTED"), 1491377495212L));
        events.get(0).setHost("12345");
        events.get(0).setType("APPLICATION_LOG");
        events.add(new SingleEvent("scsmbstgrb", StateEnum.valueOf("STARTED"), 1491377495213L));
        events.add(new SingleEvent("scsmbstgrc", StateEnum.valueOf("FINISHED"), 1491377495218L));
        events.add(new SingleEvent("scsmbstgra", StateEnum.valueOf("FINISHED"), 1491377495217L));
        events.get(3).setHost("12345");
        events.get(3).setType("APPLICATION_LOG");
        events.add(new SingleEvent("scsmbstgrc", StateEnum.valueOf("STARTED"), 1491377495210L));
        events.add(new SingleEvent("scsmbstgrb", StateEnum.valueOf("FINISHED"), 1491377495216L));
        events.add(null);

        eventsQueue.addAll(events);

        try {
            service.mergeEvents();
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(4, fullEventsQueue.size());

        FullEvent expected = new FullEvent("scsmbstgra");
        expected.setStartTimestamp(1491377495212L);
        expected.setEndTimestamp(1491377495217L);
        expected.setHost("12345");
        expected.setType("APPLICATION_LOG");
        expected.setDuration(expected.getEndTimestamp() - expected.getStartTimestamp());
        expected.setAlert(expected.getDuration() > 4);

        assertEquals(expected, fullEventsQueue.poll());

        expected = new FullEvent("scsmbstgrc");
        expected.setStartTimestamp(1491377495210L);
        expected.setEndTimestamp(1491377495218L);
        expected.setDuration(expected.getEndTimestamp() - expected.getStartTimestamp());
        expected.setAlert(expected.getDuration() > 4);

        assertEquals(expected, fullEventsQueue.poll());

        expected = new FullEvent("scsmbstgrb");
        expected.setStartTimestamp(1491377495213L);
        expected.setEndTimestamp(1491377495216L);
        expected.setDuration(expected.getEndTimestamp() - expected.getStartTimestamp());
        expected.setAlert(expected.getDuration() > 4);

        assertEquals(expected, fullEventsQueue.poll());

        assertNull(fullEventsQueue.poll());
    }

    @Test
    void parseLines_threadWait_ShouldPass() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        CountDownLatch latch = new CountDownLatch(1);

        executorService.execute(() -> {
            try {
                service.mergeEvents();
            } catch (InterruptedException e) {
                fail();
            } finally {
                latch.countDown();
            }
        });

        Thread.sleep(10);

        eventsQueue.add(null);

        latch.await();

        assertEquals(1, fullEventsQueue.size());
        assertNull(fullEventsQueue.poll());

        service.maxNumWaits = 20;

        assertThrows(InterruptedException.class, () -> service.mergeEvents());

        service.maxNumWaits = 1200;
    }
}