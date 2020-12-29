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

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final int NUMBER_OF_THREADS = 4;
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private static final RelationalDataStore<Jdbi> DATA_STORE = new DefaultJDBIDataStore();
    private static final LogFileReaderService LOG_FILE_READER_SERVICE = new DefaultLogFileReaderService();
    private static final EventLineParserService EVENT_LINE_PARSER_SERVICE = new DefaultEventLineParserService();
    private static final EventMergerService EVENT_MERGER_SERVICE = new DefaultEventMergerService();
    private static final EventToDBService EVENT_TO_DB_SERVICE = new DefaultEventToDBService();

    public static void main(String[] args) throws InterruptedException {
        if (args == null || args.length != 1) {
            LOG.severe("Invalid input parameters");
            throw new IllegalArgumentException("Invalid input parameters");
        }

        String fileName = args[0];

        LOG.log(Level.INFO, "Target log file: {0}", fileName);

        File logFile = new File(fileName);
        if (!logFile.exists() || logFile.isDirectory()) {
            LOG.severe("The file provided does not exist.");
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
            } finally {
                latch.countDown();
            }
        });

        service.execute(() -> {
            try {
                EVENT_MERGER_SERVICE.mergeEvents();
            } finally {
                latch.countDown();
            }
        });

        service.execute(() -> {
            try {
                EVENT_TO_DB_SERVICE.writeEventsToDB();
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        System.exit(0);
    }
}
