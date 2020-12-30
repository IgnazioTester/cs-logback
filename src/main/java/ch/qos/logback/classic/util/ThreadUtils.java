package ch.qos.logback.classic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadUtils.class);

    private ThreadUtils() {
    }

    public static void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.warn("An error occurred while thread was sleeping.");
            Thread.currentThread().interrupt();
        }
    }
}
