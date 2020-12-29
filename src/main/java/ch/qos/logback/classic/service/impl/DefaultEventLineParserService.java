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

import java.util.Queue;
import java.util.logging.Logger;

public class DefaultEventLineParserService implements EventLineParserService {
    private static final Logger LOG = Logger.getLogger(DefaultEventLineParserService.class.getName());

    private final ObjectMapper mapper = new ObjectMapper();

    private Queue<StringEvent> linesQueue = DefaultMessageQueue.getInstance().getQueueForClass(StringEvent.class);
    private Queue<SingleEvent> eventsQueue = DefaultMessageQueue.getInstance().getQueueForClass(SingleEvent.class);

    @Override
    public void parseLines() {
        boolean finished = false;
        while (!finished) {
            if (linesQueue.isEmpty()) {
                ThreadUtils.safeSleep();
                continue;
            }

            StringEvent event = linesQueue.poll();

            if (event == null)
                finished = true;
            else
                parseLine(event.getEvent());
        }

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
            LOG.warning("Line wasn't proper JSON format, skipping.");
            return;
        }

        if ((element = node.get("id")) == null)
            LOG.warning("Found log entry without an Id, skipping.");
        else
            id = element.asText();

        if ((element = node.get("state")) == null)
            LOG.warning("Found log entry without a state, skipping.");
        else
            state = element.asText();

        if ((element = node.get("timestamp")) == null ||
                element.asLong(-1) == -1)
            LOG.warning("Found log entry without a timestamp, skipping.");
        else
            timestamp = element.asLong();

        if ((element = node.get("host")) != null)
            host = element.asText();

        if ((element = node.get("type")) != null)
            type = element.asText();

        SingleEvent event = new SingleEvent(id, StateEnum.valueOf(state), timestamp);
        event.setHost(host);
        event.setType(type);

        eventsQueue.add(event);
    }
}
