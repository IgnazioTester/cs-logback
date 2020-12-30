package ch.qos.logback.classic.service.impl;

import ch.qos.logback.classic.enums.StateEnum;
import ch.qos.logback.classic.model.SingleEvent;
import ch.qos.logback.classic.model.StringEvent;
import ch.qos.logback.classic.queue.impl.DefaultMessageQueue;
import ch.qos.logback.classic.service.EventLineParserService;
import ch.qos.logback.classic.util.ThreadUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

public class DefaultEventLineParserService implements EventLineParserService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventLineParserService.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private Queue<StringEvent> linesQueue = DefaultMessageQueue.getInstance().getQueueForClass(StringEvent.class);
    private Queue<SingleEvent> eventsQueue = DefaultMessageQueue.getInstance().getQueueForClass(SingleEvent.class);

    int maxNumWaits = 1200;

    @Override
    public void parseLines() throws InterruptedException {
        boolean finished = false;
        int waitCount = 0;

        while (!finished) {
            if (waitCount >= maxNumWaits) { // 200 waits of 50 millis equals 10.000 millis, 10 seconds
                throw new InterruptedException("EventLineParserService did not receive an event for 1 minute. Shuting down.");
            } else if (linesQueue.isEmpty()) {
                ThreadUtils.safeSleep(50L);
                waitCount++;
                continue;
            }

            waitCount = 0;

            StringEvent event = linesQueue.poll();

            if (event == null)
                finished = true;
            else
                parseLine(event.getEvent());
        }

        LOG.info("All lines has been parsed into events.");

        eventsQueue.add(null);
    }

    private void parseLine(String line) {
        String id = "";
        String state = "";
        String type = "";
        String host = "";
        long timestamp = -1;

        JsonNode node;
        JsonNode element;

        try {
            node = mapper.readTree(line);
        } catch (JsonProcessingException e) {
            LOG.warn("Line wasn't proper JSON format, skipping.");
            return;
        }

        if ((element = node.get("id")) == null) {
            LOG.warn("Found log entry without an Id, skipping.");
            return;
        } else
            id = element.asText();

        if ((element = node.get("state")) == null) {
            LOG.warn("Found log entry without a state, skipping. Id was {}", id);
            return;
        } else
            state = element.asText();

        if ((element = node.get("timestamp")) == null ||
                element.asLong(-1) == -1) {
            LOG.warn("Found log entry without a timestamp, skipping. Id was {}", id);
            return;
        } else
            timestamp = element.asLong();

        if ((element = node.get("host")) != null)
            host = element.asText();

        if ((element = node.get("type")) != null)
            type = element.asText();

        StateEnum stateEnumValue;
        try {
            stateEnumValue = StateEnum.valueOf(state);
        } catch (IllegalArgumentException e) {
            LOG.warn("Found log entry with an unknown state {}.", state);
            return;
        }

        SingleEvent event = new SingleEvent(id, stateEnumValue, timestamp);
        event.setHost(host);
        event.setType(type);

        eventsQueue.add(event);

        LOG.debug("Parsed event {} for state {}", event.getId(), event.getState());
    }
}
