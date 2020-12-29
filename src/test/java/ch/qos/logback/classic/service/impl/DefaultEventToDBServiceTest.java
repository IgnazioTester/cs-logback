package ch.qos.logback.classic.service.impl;

import ch.qos.logback.classic.model.FullEvent;
import ch.qos.logback.classic.queue.impl.DefaultMessageQueue;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultEventToDBServiceTest {
    static DefaultEventToDBService service = new DefaultEventToDBService();

    private final static Jdbi DATA_STORE = mock(Jdbi.class);

    Queue<FullEvent> eventsQueue = DefaultMessageQueue.getInstance().getQueueForClass(FullEvent.class);

    @BeforeAll
    static void setUp() {
        service.dataStore = DATA_STORE;
        doNothing().when(DATA_STORE).useHandle(any());
    }

    @Test
    void writeEventsToDB_ValidData_ShouldPass() {
        FullEvent fullEvent = new FullEvent("scsmbstgra");
        fullEvent.setStartTimestamp(1491377495212L);
        fullEvent.setEndTimestamp(1491377495217L);
        fullEvent.setHost("12345");
        fullEvent.setType("APPLICATION_LOG");
        fullEvent.setDuration(fullEvent.getEndTimestamp() - fullEvent.getStartTimestamp());
        fullEvent.setAlert(fullEvent.getDuration() > 4);

        eventsQueue.add(fullEvent);

        fullEvent = new FullEvent("scsmbstgrc");
        fullEvent.setStartTimestamp(1491377495210L);
        fullEvent.setEndTimestamp(1491377495218L);
        fullEvent.setDuration(fullEvent.getEndTimestamp() - fullEvent.getStartTimestamp());
        fullEvent.setAlert(fullEvent.getDuration() > 4);

        eventsQueue.add(fullEvent);

        fullEvent = new FullEvent("scsmbstgrb");
        fullEvent.setStartTimestamp(1491377495213L);
        fullEvent.setEndTimestamp(1491377495216L);
        fullEvent.setDuration(fullEvent.getEndTimestamp() - fullEvent.getStartTimestamp());
        fullEvent.setAlert(fullEvent.getDuration() > 4);

        eventsQueue.add(fullEvent);

        eventsQueue.add(null);

        service.writeEventsToDB();

        verify(DATA_STORE, times(3)).useHandle(any());
    }

    @Test
    void parseLines_threadWait_ShouldPass() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        CountDownLatch latch = new CountDownLatch(1);

        executorService.execute(() -> {
            try {
                service.writeEventsToDB();
            } finally {
                latch.countDown();
            }
        });

        Thread.sleep(10);

        eventsQueue.add(null);

        latch.await();
    }
}