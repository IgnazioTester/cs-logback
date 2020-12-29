package ch.qos.logback.classic.service.impl;

import ch.qos.logback.classic.model.SingleEvent;
import ch.qos.logback.classic.model.StringEvent;
import ch.qos.logback.classic.queue.impl.DefaultMessageQueue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class DefaultEventLineParserServiceTest {
    DefaultEventLineParserService service = new DefaultEventLineParserService();

    Queue<StringEvent> linesQueue = DefaultMessageQueue.getInstance().getQueueForClass(StringEvent.class);
    Queue<SingleEvent> eventsQueue = DefaultMessageQueue.getInstance().getQueueForClass(SingleEvent.class);

    @Test
    void parseLines_validContent_ShouldPass() {
        List<StringEvent> events = new LinkedList<>();

        events.add(new StringEvent("{\"id\":\"scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}"));
        events.add(new StringEvent("{\"id\":\"scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}"));
        events.add(new StringEvent("{\"id\":\"scsmbstgrc\", \"state\":\"FINISHED\", \"timestamp\":1491377495218}"));
        events.add(new StringEvent("{\"id\":\"scsmbstgra\", \"state\":\"FINISHED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495217}"));
        events.add(new StringEvent("{\"id\":\"scsmbstgrc\", \"state\":\"STARTED\", \"timestamp\":1491377495210}"));
        events.add(new StringEvent("{\"id\":\"scsmbstgrb\", \"state\":\"FINISHED\", \"timestamp\":1491377495216}"));
        events.add(null);

        linesQueue.addAll(events);

        service.parseLines();

        assertEquals(events.size(), eventsQueue.size());

        for (int i = 0; i < events.size() - 1; i++) {
            String expected = events.get(i).getEvent();
            SingleEvent actual = eventsQueue.poll();

            assertTrue(expected.contains(actual.getId()));
            assertTrue(expected.contains(actual.getState().name()));
            assertTrue(expected.contains(String.valueOf(actual.getTimestamp())));
            if (StringUtils.isNotBlank(actual.getHost()))
                assertTrue(expected.contains(actual.getHost()));
            if (StringUtils.isNotBlank(actual.getType()))
                assertTrue(expected.contains(actual.getType()));
        }

        assertNull(eventsQueue.poll());
    }
}