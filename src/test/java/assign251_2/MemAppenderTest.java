package assign251_2;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MemAppenderTest {

    private MemAppender memAppender;

    @BeforeEach
    void setUp() {
        // Reset the singleton instance before each test
        MemAppender.resetInstance();

        // Create a fresh MemAppender instance with maxSize of 3 for testing
        memAppender = MemAppender.createAppender("TestAppender", PatternLayout.newBuilder().withPattern("%m").build(), null, 3);
    }

    @Test
    void testLogEventDiscarding() {
        // Create mock log events
        LogEvent event1 = mock(LogEvent.class);
        LogEvent event2 = mock(LogEvent.class);
        LogEvent event3 = mock(LogEvent.class);
        LogEvent event4 = mock(LogEvent.class);

        // Add 3 events to fill up the appender
        memAppender.append(event1);
        memAppender.append(event2);
        memAppender.append(event3);

        // Check that no logs have been discarded yet
        assertEquals(0, memAppender.getDiscardedLogCount());

        // Add 4th event, which should cause the first event (event1) to be discarded
        memAppender.append(event4);

        // Now, one log should be discarded
        assertEquals(1, memAppender.getDiscardedLogCount());
    }

    @Test
    void testDiscardedLogCount() {
        // Create mock log events
        LogEvent event1 = mock(LogEvent.class);
        LogEvent event2 = mock(LogEvent.class);
        LogEvent event3 = mock(LogEvent.class);
        LogEvent event4 = mock(LogEvent.class);

        // Add exactly 4 events to trigger one discard
        memAppender.append(event1); // log 1
        memAppender.append(event2); // log 2
        memAppender.append(event3); // log 3
        memAppender.append(event4); // log 4 - should discard event1

        // Check the discarded log count is exactly 1
        assertEquals(1, memAppender.getDiscardedLogCount());
    }
}
