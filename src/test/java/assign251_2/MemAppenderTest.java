package assign251_2;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MemAppenderTest {

    @BeforeEach
    void setUp() {
        // Reset the singleton instance before each test
        MemAppender.resetInstance();
    }

    @Test
    void testMemAppenderWithArrayList() {
        // Inject an ArrayList
        List<LogEvent> arrayList = new ArrayList<>();
        MemAppender appender = MemAppender.getInstance("TestAppender", null, null, 3, arrayList);

        // Append 5 log events; the appender can only hold 3 at a time
        appender.append(mockLogEvent("Log 1"));
        appender.append(mockLogEvent("Log 2"));
        appender.append(mockLogEvent("Log 3"));
        appender.append(mockLogEvent("Log 4"));  // Discards "Log 1"
        appender.append(mockLogEvent("Log 5"));  // Discards "Log 2"

        // Assert that only 3 logs are stored
        assertEquals(3, appender.getCurrentLogs().size(), "Current logs should be 3");

        // Assert that 2 logs have been discarded
        assertEquals(2, appender.getDiscardedLogCount(), "Two logs should have been discarded");
    }

    @Test
    void testMemAppenderWithLinkedList() {
        // Inject a LinkedList
        List<LogEvent> linkedList = new LinkedList<>();
        MemAppender appender = MemAppender.getInstance("TestAppender", null, null, 3, linkedList);

        appender.append(mockLogEvent("Log A"));
        appender.append(mockLogEvent("Log B"));
        appender.append(mockLogEvent("Log C"));
        appender.append(mockLogEvent("Log D"));  // Discards "Log A"

        assertEquals(3, appender.getCurrentLogs().size(), "Current logs should be 3");
        assertEquals(1, appender.getDiscardedLogCount(), "One log should have been discarded");
    }

    @Test
    void testGetEventStringsAndPrintLogs() {
        // Set up MemAppender with a layout
        PatternLayout layout = PatternLayout.newBuilder().withPattern("%m%n").build();
        MemAppender appender = MemAppender.getInstance("TestAppender", layout, null, 10);
        appender.start();  // Start the appender

        // Append some log events
        appender.append(mockLogEvent("Message 1"));
        appender.append(mockLogEvent("Message 2"));

        // Test getEventStrings()
        List<String> eventStrings = appender.getEventStrings();
        assertEquals(2, eventStrings.size());
        assertEquals("Message 1\n", eventStrings.get(0));
        assertEquals("Message 2\n", eventStrings.get(1));

        // Test printLogs()
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        appender.printLogs();

        System.setOut(originalOut);

        String output = outContent.toString();
        assertTrue(output.contains("Message 1"));
        assertTrue(output.contains("Message 2"));

        assertEquals(0, appender.getCurrentLogs().size(), "Logs should be cleared after printing");
    }

    @Test
    void testExceptionWhenLayoutNotSet() {
        MemAppender appender = MemAppender.getInstance("TestAppender", null, null, 3);

        // Append some log events
        appender.append(mockLogEvent("Test message"));

        // Try to call getEventStrings() without setting a layout
        Exception exception = assertThrows(IllegalStateException.class, appender::getEventStrings);
        assertEquals("Layout is not set.", exception.getMessage());
    }

    @Test
    void testAppenderWithDifferentLogLevels() {
        // Set up MemAppender with a layout
        PatternLayout layout = PatternLayout.newBuilder().withPattern("[%p] %m%n").build();
        MemAppender appender = MemAppender.getInstance("TestAppender", layout, null, 10);
        appender.start();  // Start the appender

        // Attach MemAppender to logger
        Logger logger = LogManager.getLogger("TestLogger");
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());

        // Remove existing appenders and add MemAppender
        loggerConfig.removeAppender("TestAppender");
        loggerConfig.addAppender(appender, Level.ALL, null);

        // Set the logger level to ALL
        loggerConfig.setLevel(Level.ALL);

        context.updateLoggers();  // Apply changes

        // Log messages at different levels
        logger.debug("Debug message");
        logger.info("Info message");
        logger.error("Error message");

        // Verify that all messages are captured
        List<String> eventStrings = appender.getEventStrings();
        assertEquals(3, eventStrings.size(), "Expected 3 log events, but found " + eventStrings.size());
        assertTrue(eventStrings.get(0).contains("[DEBUG]"));
        assertTrue(eventStrings.get(0).contains("Debug message"));
        assertTrue(eventStrings.get(1).contains("[INFO]"));
        assertTrue(eventStrings.get(1).contains("Info message"));
        assertTrue(eventStrings.get(2).contains("[ERROR]"));
        assertTrue(eventStrings.get(2).contains("Error message"));
    }

    @Test
    void testResetMethod() {
        MemAppender appender = MemAppender.getInstance("TestAppender", null, null, 5);
        appender.append(mockLogEvent("Log 1"));
        appender.append(mockLogEvent("Log 2"));

        appender.reset();

        assertEquals(0, appender.getCurrentLogs().size(), "Logs should be cleared after reset");
        assertEquals(0, appender.getDiscardedLogCount(), "Discarded log count should be reset");
    }

    @Test
    void testSetAndGetLayout() {
        MemAppender appender = MemAppender.getInstance("TestAppender", null, null, 5);
        Layout<? extends Serializable> layout = PatternLayout.newBuilder().withPattern("%m%n").build();
        appender.setLayout(layout);

        assertEquals(layout, appender.getLayout(), "Layout should be set correctly");
    }

    @Test
    void testAppenderWithZeroMaxSize() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            MemAppender.getInstance("TestAppenderZero", null, null, 0);
        });
        assertEquals("maxSize must be greater than zero", exception.getMessage());
    }

    @Test
    void testAppenderWithNegativeMaxSize() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            MemAppender.getInstance("TestAppenderNegative", null, null, -5);
        });
        assertEquals("maxSize must be greater than zero", exception.getMessage());
    }

    @Test
    void testAppendNullEvent() {
        MemAppender appender = MemAppender.getInstance("TestAppender", null, null, 5);
        Exception exception = assertThrows(NullPointerException.class, () -> {
            appender.append(null);
        });
        assertEquals("LogEvent cannot be null", exception.getMessage());
    }

    @Test
    void testGetInstanceSingleton() {
        MemAppender appender1 = MemAppender.getInstance("TestAppender", null, null, 5);
        MemAppender appender2 = MemAppender.getInstance("TestAppender", null, null, 5);

        assertSame(appender1, appender2, "Both instances should be the same due to singleton pattern");
    }

    @Test
    void testPrintLogsWithNonStringLayout() {
        Layout<Serializable> customLayout = new Layout<Serializable>() {
            @Override
            public byte[] toByteArray(LogEvent event) {
                return ("Custom: " + event.getMessage().getFormattedMessage()).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public Serializable toSerializable(LogEvent event) {
                return "Custom: " + event.getMessage().getFormattedMessage();
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }

            @Override
            public Map<String, String> getContentFormat() {
                return null;
            }

            @Override
            public byte[] getFooter() {
                return null;
            }

            @Override
            public byte[] getHeader() {
                return null;
            }

            @Override
            public void encode(LogEvent event, ByteBufferDestination destination) {
                byte[] data = toByteArray(event);
                destination.writeBytes(data, 0, data.length);
            }
        };

        MemAppender appender = MemAppender.getInstance("TestAppender", customLayout, null, 5);
        appender.start();
        appender.append(mockLogEvent("Test message"));

        // Capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        appender.printLogs();

        System.setOut(originalOut);

        String output = outContent.toString();
        assertTrue(output.contains("Custom: Test message"), "Output should contain custom formatted message");
        assertEquals(0, appender.getCurrentLogs().size(), "Logs should be cleared after printing");
    }

    @Test
    void testPrintLogsWhenNoLogs() {
        PatternLayout layout = PatternLayout.newBuilder().withPattern("%m%n").build();
        MemAppender appender = MemAppender.getInstance("TestAppender", layout, null, 5);
        appender.start();

        // Capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        appender.printLogs();

        System.setOut(originalOut);

        String output = outContent.toString();
        assertTrue(output.isEmpty(), "Output should be empty when there are no logs");
    }

    @Test
    void testPrintLogsWhenLayoutNotSet() {
        MemAppender appender = MemAppender.getInstance("TestAppender", null, null, 5);
        appender.append(mockLogEvent("Test message"));

        Exception exception = assertThrows(IllegalStateException.class, appender::printLogs);
        assertEquals("Layout is not set.", exception.getMessage());
    }

    @Test
    void testGetInstanceWhenAlreadyExists() {
        MemAppender.resetInstance();
        MemAppender appender1 = MemAppender.getInstance("TestAppender", null, null, 5);
        MemAppender appender2 = MemAppender.getInstance("TestAppender", null, null, 5);

        assertSame(appender1, appender2, "Should return the existing instance");
    }

    @Test
    void testGetInstanceWithListWhenAlreadyExists() {
        MemAppender.resetInstance();
        List<LogEvent> customList1 = new ArrayList<>();
        MemAppender appender1 = MemAppender.getInstance("TestAppender", null, null, 5, customList1);

        List<LogEvent> customList2 = new LinkedList<>();
        MemAppender appender2 = MemAppender.getInstance("TestAppender", null, null, 5, customList2);

        assertSame(appender1, appender2, "Should return the existing instance");

        // Append a log event
        LogEvent event = mockLogEvent("Test message");
        appender1.append(event);

        // Verify that the event is in customList1
        assertTrue(customList1.contains(event), "Custom list should contain the appended event");

        // Verify that customList2 is empty
        assertEquals(0, customList2.size(), "Second custom list should not be used");
    }

    @Test
    void testGetEventStringsWithNonStringLayout() {
        Layout<Serializable> customLayout = new Layout<Serializable>() {
            @Override
            public byte[] toByteArray(LogEvent event) {
                return ("Custom: " + event.getMessage().getFormattedMessage()).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public Serializable toSerializable(LogEvent event) {
                return "Custom: " + event.getMessage().getFormattedMessage();
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }

            @Override
            public Map<String, String> getContentFormat() {
                return null;
            }

            @Override
            public byte[] getFooter() {
                return null;
            }

            @Override
            public byte[] getHeader() {
                return null;
            }

            @Override
            public void encode(LogEvent event, ByteBufferDestination destination) {
                byte[] data = toByteArray(event);
                destination.writeBytes(data, 0, data.length);
            }
        };

        MemAppender appender = MemAppender.getInstance("TestAppender", customLayout, null, 5);
        appender.start();
        appender.append(mockLogEvent("Test message"));

        List<String> eventStrings = appender.getEventStrings();

        assertEquals(1, eventStrings.size(), "Should have one event string");
        assertEquals("Custom: Test message", eventStrings.get(0), "Event string should match custom layout output");
    }

    @Test
    void testGetInstanceWithNullName() {
        MemAppender.resetInstance();
        MemAppender appender = MemAppender.getInstance(null, null, null, 5);
        assertEquals("MemAppender", appender.getName(), "Appender name should default to 'MemAppender'");
    }

    @Test
    void testGetInstanceWithNullNameAndList() {
        MemAppender.resetInstance();
        List<LogEvent> customList = new ArrayList<>();
        MemAppender appender = MemAppender.getInstance(null, null, null, 5, customList);
        assertEquals("MemAppender", appender.getName(), "Appender name should default to 'MemAppender'");
    }

    @Test
    void testGetInstanceWithNullLogEventsList() {
        MemAppender.resetInstance();
        MemAppender appender = MemAppender.getInstance("TestAppender", null, null, 5, null);

        // Append a log event
        LogEvent event = mockLogEvent("Test message");
        appender.append(event);

        // Verify that the internal list is not null and contains the event
        List<LogEvent> currentLogs = appender.getCurrentLogs();
        assertNotNull(currentLogs, "Current logs should not be null");
        assertEquals(1, currentLogs.size(), "Current logs should contain one event");
    }

    private LogEvent mockLogEvent(String message) {
        LogEvent logEvent = mock(LogEvent.class);
        when(logEvent.getMessage()).thenReturn(new SimpleMessage(message));
        when(logEvent.toImmutable()).thenReturn(logEvent);  // Ensure toImmutable() returns the mock itself
        return logEvent;
    }
}
