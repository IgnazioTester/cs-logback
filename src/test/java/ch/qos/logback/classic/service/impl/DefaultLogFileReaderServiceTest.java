package ch.qos.logback.classic.service.impl;

import ch.qos.logback.classic.model.StringEvent;
import ch.qos.logback.classic.queue.impl.DefaultMessageQueue;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class DefaultLogFileReaderServiceTest {
    final String FILE_NAME = "sample/logback.txt";

    DefaultLogFileReaderService service = new DefaultLogFileReaderService();
    Queue<StringEvent> queue = DefaultMessageQueue.getInstance().getQueueForClass(StringEvent.class);

    @Test
    void parseFile_validFile_ShouldPass() {
        service.parseFile(FILE_NAME);

        assertEquals(6 + 1, queue.size());

        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            while ((line = reader.readLine()) != null) {
                assertEquals(line, queue.poll().getEvent());
            }
        } catch (IOException e) {
            fail();
        }

        assertNull(queue.poll());
    }

    @Test
    void parseLines_invalidFile_ShouldPass() {
        service.parseFile("fake.txt");

        assertEquals(1, queue.size());
        assertNull(queue.poll());
    }
}