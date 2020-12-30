package ch.qos.logback.classic.service.impl;

import ch.qos.logback.classic.enums.StateEnum;
import ch.qos.logback.classic.model.FullEvent;
import ch.qos.logback.classic.model.SingleEvent;
import ch.qos.logback.classic.queue.impl.DefaultMessageQueue;
import ch.qos.logback.classic.service.EventMergerService;
import ch.qos.logback.classic.util.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class DefaultEventMergerService implements EventMergerService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventMergerService.class);

    private final Map<String, FullEvent> eventsMap = new HashMap<>();

    private Queue<SingleEvent> eventsQueue = DefaultMessageQueue.getInstance().getQueueForClass(SingleEvent.class);
    private Queue<FullEvent> fullEventsQueue = DefaultMessageQueue.getInstance().getQueueForClass(FullEvent.class);

    @Override
    public void mergeEvents() throws InterruptedException {
        boolean finished = false;
        int waitCount = 0;

        while (!finished) {
            if (waitCount >= 200) { // 200 waits of 50 millis equals 10.000 millis, 10 seconds
                throw new InterruptedException("EventMergerService did not receive an event for 1 minute. Shuting down.");
            } else if (eventsQueue.isEmpty()) {
                ThreadUtils.safeSleep(50L);
                waitCount++;
                continue;
            }

            waitCount = 0;

            SingleEvent event = eventsQueue.poll();

            if (event == null)
                finished = true;
            else
                parseEvent(event);
        }

        LOG.info("All single events have been merged.");

        fullEventsQueue.add(null);
    }

    private void parseEvent(SingleEvent event) {
        FullEvent fullEvent;
        boolean found = false;

        if (eventsMap.containsKey(event.getId())) {
            fullEvent = eventsMap.get(event.getId());
            found = true;
        } else {
            fullEvent = new FullEvent(event.getId());
            eventsMap.put(event.getId(), fullEvent);
        }

        if (StateEnum.STARTED.equals(event.getState())) {
            fullEvent.setStartTimestamp(event.getTimestamp());
        } else if (StateEnum.FINISHED.equals(event.getState())) {
            fullEvent.setEndTimestamp(event.getTimestamp());
        } else {
            LOG.error("Event {} state was not recognised: {}", event.getId(), event.getState());
            return;
        }

        fullEvent.setHost(event.getHost());
        fullEvent.setType(event.getType());

        if (found) {
            long duration = fullEvent.getEndTimestamp() - fullEvent.getStartTimestamp();
            fullEvent.setDuration(duration);
            fullEvent.setAlert(duration > 4);

            eventsMap.remove(fullEvent.getId());

            fullEventsQueue.add(fullEvent);

            LOG.debug("Parsed event: {}", fullEvent);
        }
    }
}
