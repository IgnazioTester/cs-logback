package ch.qos.logback.classic.util;

import java.util.logging.Logger;

public class ThreadUtils {
    private static final Logger LOG = Logger.getLogger(ThreadUtils.class.getName());

    private ThreadUtils() {
    }

    public static void safeSleep() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            LOG.warning("An error occurred while thread was sleeping.");
            Thread.currentThread().interrupt();
        }
    }
}
