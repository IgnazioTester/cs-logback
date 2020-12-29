package ch.qos.logback.classic.service.impl;

import ch.qos.logback.classic.enums.StateEnum;
import ch.qos.logback.classic.model.FullEvent;
import ch.qos.logback.classic.model.SingleEvent;
import ch.qos.logback.classic.queue.impl.DefaultMessageQueue;
import ch.qos.logback.classic.service.EventMergerService;
import ch.qos.logback.classic.util.ThreadUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

public class DefaultEventMergerService implements EventMergerService {
    private static final Logger LOG = Logger.getLogger(DefaultEventMergerService.class.getName());

    private final Map<String, FullEvent> eventsMap = new HashMap<>();

    private Queue<SingleEvent> eventsQueue = DefaultMessageQueue.getInstance().getQueueForClass(SingleEvent.class);
    private Queue<FullEvent> fullEventsQueue = DefaultMessageQueue.getInstance().getQueueForClass(FullEvent.class);

    @Override
    public void mergeEvents() {
        boolean finished = false;

        while (!finished) {
            if (eventsQueue.isEmpty()) {
                ThreadUtils.safeSleep();
                continue;
            }

            SingleEvent event = eventsQueue.poll();

            if (event == null)
                finished = true;
            else
                parseEvent(event);
        }

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
        } else {
            fullEvent.setEndTimestamp(event.getTimestamp());
        }

        fullEvent.setHost(event.getHost());
        fullEvent.setType(event.getType());

        if (found) {
            long duration = fullEvent.getEndTimestamp() - fullEvent.getStartTimestamp();
            fullEvent.setDuration(duration);
            fullEvent.setAlert(duration > 4);

            eventsMap.remove(fullEvent.getId());

            fullEventsQueue.add(fullEvent);
        }
    }
}
