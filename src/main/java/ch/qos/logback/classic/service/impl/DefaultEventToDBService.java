package ch.qos.logback.classic.service.impl;

import ch.qos.logback.classic.data.store.impl.DefaultJDBIDataStore;
import ch.qos.logback.classic.model.FullEvent;
import ch.qos.logback.classic.queue.impl.DefaultMessageQueue;
import ch.qos.logback.classic.service.EventToDBService;
import ch.qos.logback.classic.util.ThreadUtils;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

public class DefaultEventToDBService implements EventToDBService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventToDBService.class);

    private static final String INSERT_EVENT_QUERY = "INSERT INTO LOGBACK (Event_Id, Duration, Type, Host, Alert) VALUES (:eventId, :duration, :type, :host, :alert)";

    Jdbi dataStore = new DefaultJDBIDataStore().getDataStore();
    private Queue<FullEvent> eventsQueue = DefaultMessageQueue.getInstance().getQueueForClass(FullEvent.class);

    @Override
    public void writeEventsToDB() {
        boolean finished = false;

        while (!finished) {
            if (eventsQueue.isEmpty()) {
                ThreadUtils.safeSleep();
                continue;
            }

            FullEvent event = eventsQueue.poll();

            if (event == null)
                finished = true;
            else
                writeEventToDB(event);
        }

        LOG.info("All events have been written to database.");
    }

    private void writeEventToDB(FullEvent event) {
        dataStore.useHandle(handle ->
            handle.createUpdate(INSERT_EVENT_QUERY)
                    .bind("eventId", event.getId())
                    .bind("duration", event.getDuration())
                    .bind("type", event.getType())
                    .bind("host", event.getHost())
                    .bind("alert", event.isAlert())
                    .execute()
        );

        LOG.debug("Wrote to DB event: {}", event.getId());
    }
}
