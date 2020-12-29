package ch.qos.logback.classic.service.impl;

import ch.qos.logback.classic.model.StringEvent;
import ch.qos.logback.classic.queue.impl.DefaultMessageQueue;
import ch.qos.logback.classic.service.LogFileReaderService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;
import java.util.logging.Logger;

public class DefaultLogFileReaderService implements LogFileReaderService {
    private static final Logger LOG = Logger.getLogger(DefaultLogFileReaderService.class.getName());

    private final Queue<StringEvent> queue = DefaultMessageQueue.getInstance().getQueueForClass(StringEvent.class);

    @Override
    public void parseFile(String fileName) {
        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while ((line = reader.readLine()) != null) {
                queue.add(new StringEvent(line));
            }
        } catch (IOException e) {
            LOG.severe("An error occurred while reading the log file.");
        } finally {
            queue.add(null);
        }
    }
}
