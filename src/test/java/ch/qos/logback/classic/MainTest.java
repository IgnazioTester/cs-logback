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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Permission;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MainTest {

    static RelationalDataStore<Jdbi> DATA_STORE = mock(DefaultJDBIDataStore.class);
    static LogFileReaderService LOG_FILE_READER_SERVICE = mock(DefaultLogFileReaderService.class);
    static EventLineParserService EVENT_LINE_PARSER_SERVICE = mock(DefaultEventLineParserService.class);
    static EventMergerService EVENT_MERGER_SERVICE = mock(DefaultEventMergerService.class);
    static EventToDBService EVENT_TO_DB_SERVICE = mock(DefaultEventToDBService.class);

    @BeforeAll
    static void setUp() {
        System.setSecurityManager(new NoExitSecurityManager());

        Main.DATA_STORE = DATA_STORE;
        Main.LOG_FILE_READER_SERVICE = LOG_FILE_READER_SERVICE;
        Main.EVENT_LINE_PARSER_SERVICE = EVENT_LINE_PARSER_SERVICE;
        Main.EVENT_MERGER_SERVICE = EVENT_MERGER_SERVICE;
        Main.EVENT_TO_DB_SERVICE = EVENT_TO_DB_SERVICE;
    }

    @AfterAll
    static void tearDown() {
        System.setSecurityManager(null);
    }

    @Test
    void main_valid_shouldPass() throws InterruptedException {
        doNothing().when(DATA_STORE).clearTables();
        doNothing().when(LOG_FILE_READER_SERVICE).parseFile(any());
        doNothing().when(EVENT_LINE_PARSER_SERVICE).parseLines();
        doNothing().when(EVENT_MERGER_SERVICE).mergeEvents();
        doNothing().when(EVENT_TO_DB_SERVICE).writeEventsToDB();

        assertThrows(ExitException.class, () -> Main.main(new String[] {"sample/logback.txt"}));
    }

    @Test
    void main_invalid_shouldPass() {
        assertThrows(IllegalArgumentException.class, () -> Main.main(null));
        assertThrows(IllegalArgumentException.class, () -> Main.main(new String[0]));
        assertThrows(IllegalArgumentException.class, () -> Main.main(new String[] {"fake.txt"}));
    }

    protected static class ExitException extends SecurityException
    {
        public final int status;
        public ExitException(int status)
        {
            super("There is no escape!");
            this.status = status;
        }
    }

    private static class NoExitSecurityManager extends SecurityManager
    {
        @Override
        public void checkPermission(Permission perm)
        {
            // allow anything.
        }
        @Override
        public void checkPermission(Permission perm, Object context)
        {
            // allow anything.
        }
        @Override
        public void checkExit(int status)
        {
            super.checkExit(status);
            throw new ExitException(status);
        }
    }
}