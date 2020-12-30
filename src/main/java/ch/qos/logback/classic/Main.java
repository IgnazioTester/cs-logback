package ch.qos.logback.classic;

import ch.qos.logback.classic.data.store.RelationalDataStore;
import ch.qos.logback.classic.data.store.impl.DefaultJDBIDataStore;
import ch.qos.logback.classic.service.EventLineParserService;
import ch.qos.logback.classic.service.EventMergerService;
import ch.qos.logback.classic.service.EventToDBService;
import ch.qos.logback.classic.service.LogFileReaderService;
import ch.qos.logback.classic.service.impl.DefaultEventLineParserService;
import ch.qos.logback.classic.service.impl.DefaultEventMergerService;
import ch.qos.logback.classic.service.impl.DefaultEventToDBService;
import ch.qos.logback.classic.service.impl.DefaultLogFileReaderService;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.Permission;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class Main {
    private static final int NUMBER_OF_THREADS = 4;
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    static RelationalDataStore<Jdbi> DATA_STORE = new DefaultJDBIDataStore();
    static LogFileReaderService LOG_FILE_READER_SERVICE = new DefaultLogFileReaderService();
    static EventLineParserService EVENT_LINE_PARSER_SERVICE = new DefaultEventLineParserService();
    static EventMergerService EVENT_MERGER_SERVICE = new DefaultEventMergerService();
    static EventToDBService EVENT_TO_DB_SERVICE = new DefaultEventToDBService();

    public static void main(String[] args) throws InterruptedException {
        if (args == null || args.length != 1) {
            LOG.error("Invalid input parameters");
            throw new IllegalArgumentException("Invalid input parameters");
        }

        String fileName = args[0];

        LOG.info("Target log file: {}", fileName);

        File logFile = new File(fileName);
        if (!logFile.exists() || logFile.isDirectory()) {
            LOG.error("The file provided does not exist.");
            throw new IllegalArgumentException("The file provided does not exist.");
        }

        DATA_STORE.clearTables();

        ExecutorService service = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS);

        service.execute(() -> {
            try {
                LOG_FILE_READER_SERVICE.parseFile(fileName);
            } finally {
                latch.countDown();
            }
        });

        service.execute(() -> {
            try {
                EVENT_LINE_PARSER_SERVICE.parseLines();
            } catch (InterruptedException e) {
                LOG.error("Event line parser server hanged up and shutdown after a minute without input");
                throw new RejectedExecutionException("Event line parser server hanged up and shutdown after a minute without input");
            } finally {
                latch.countDown();
            }
        });

        service.execute(() -> {
            try {
                EVENT_MERGER_SERVICE.mergeEvents();
            } catch (InterruptedException e) {
                LOG.error("Event merger server hanged up and shutdown after a minute without input");
                throw new RejectedExecutionException("Event merger server hanged up and shutdown after a minute without input");
            } finally {
                latch.countDown();
            }
        });

        service.execute(() -> {
            try {
                EVENT_TO_DB_SERVICE.writeEventsToDB();
            } catch (InterruptedException e) {
                LOG.error("Event to DB server hanged up and shutdown after a minute without input");
                throw new RejectedExecutionException("Event to DB server hanged up and shutdown after a minute without input");
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        System.exit(0);
    }
}
