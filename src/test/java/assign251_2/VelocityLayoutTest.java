package assign251_2;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;


import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VelocityLayoutTest {

    @Test
    void testVelocityLayoutFormatting() {
        // Create a simple log event
        LogEvent event = mockLogEvent("Hello, Velocity!", Level.INFO);

        // Velocity pattern for testing
        String pattern = "[$p] $c $d: $m$n";

        // Create a VelocityLayout with the test pattern
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, pattern);

        // Apply the layout and verify the output
        String formatted = layout.toSerializable(event);
        assertTrue(formatted.contains("[INFO]"));
        assertTrue(formatted.contains("TestLogger"));
        assertTrue(formatted.contains("Hello, Velocity!"));
        assertTrue(formatted.contains(System.lineSeparator()));
    }

    @Test
    void testAllVariablesInPattern() {
        // Create a mock LogEvent
        LogEvent event = mock(LogEvent.class);
        when(event.getMessage()).thenReturn(new SimpleMessage("Test message"));
        when(event.getTimeMillis()).thenReturn(1627846261000L); // Fixed timestamp
        when(event.getLevel()).thenReturn(Level.WARN);
        when(event.getLoggerName()).thenReturn("TestLogger");
        when(event.getThreadName()).thenReturn("TestThread");

        // Velocity pattern using all variables
        String pattern = "$d [$p] $c ($t): $m$n";

        // Create VelocityLayout
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, pattern);

        // Format the event
        String formatted = layout.toSerializable(event);

        // Assertions
        assertTrue(formatted.contains("[WARN]"));
        assertTrue(formatted.contains("TestLogger"));
        assertTrue(formatted.contains("(TestThread)"));
        assertTrue(formatted.contains("Test message"));
        assertTrue(formatted.contains(System.lineSeparator()));
    }

    @Test
    void testNullPattern() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            VelocityLayout.createLayout(StandardCharsets.UTF_8, null);
        });
        assertEquals("Pattern cannot be null or empty.", exception.getMessage());
    }

    @Test
    void testEmptyPattern() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            VelocityLayout.createLayout(StandardCharsets.UTF_8, "");
        });
        assertEquals("Pattern cannot be null or empty.", exception.getMessage());
    }

    @Test
    void testSetPattern() {
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "$m");
        layout.setPattern("[$p] $m");

        LogEvent event = mockLogEvent("Test message", Level.INFO);
        String result = layout.toSerializable(event);

        assertEquals("[INFO] Test message", result);
    }

    @Test
    void testSetPatternWithNull() {
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "$m");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            layout.setPattern(null);
        });

        assertEquals("Pattern cannot be null or empty.", exception.getMessage());
    }

    @Test
    void testSetPatternWithEmptyString() {
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "$m");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            layout.setPattern("");
        });

        assertEquals("Pattern cannot be null or empty.", exception.getMessage());
    }

    @Test
    void testToSerializableWithNullEvent() {
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "$m");

        Exception exception = assertThrows(NullPointerException.class, () -> {
            layout.toSerializable(null);
        });

        assertEquals("LogEvent cannot be null", exception.getMessage());
    }

    @Test
    void testToSerializableWithNullMessage() {
        LogEvent event = mock(LogEvent.class);
        when(event.getMessage()).thenReturn(null);
        when(event.getTimeMillis()).thenReturn(System.currentTimeMillis());
        when(event.getLevel()).thenReturn(Level.INFO);
        when(event.getLoggerName()).thenReturn("TestLogger");
        when(event.getThreadName()).thenReturn("Thread-1");

        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "$m");
        String result = layout.toSerializable(event);

        assertEquals("null", result);
    }

    @Test
    void testUnsupportedVariablesInPattern() {
        LogEvent event = mockLogEvent("Test message", Level.INFO);
        String pattern = "$x $y";
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, pattern);

        String formatted = layout.toSerializable(event);

        assertEquals("$x $y", formatted);
    }

    @Test
    void testToByteArray() {
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "$m");
        LogEvent event = mockLogEvent("Test message", Level.INFO);

        byte[] byteArray = layout.toByteArray(event);
        String result = new String(byteArray, StandardCharsets.UTF_8);

        assertEquals("Test message", result);
    }

    @Test
    void testGetContentType() {
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "$m");
        String contentType = layout.getContentType();

        assertEquals("text/plain", contentType);
    }

    @Test
    void testSetPatternValidAfterInvalid() {
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "$m");

        // Attempt to set an invalid pattern
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            layout.setPattern(null);
        });
        assertEquals("Pattern cannot be null or empty.", exception.getMessage());

        // Now set a valid pattern
        layout.setPattern("[$p] $m");

        LogEvent event = mockLogEvent("Test message", Level.INFO);
        String result = layout.toSerializable(event);

        assertEquals("[INFO] Test message", result);
    }

    private LogEvent mockLogEvent(String message, Level level) {
        LogEvent event = mock(LogEvent.class);
        when(event.getMessage()).thenReturn(new SimpleMessage(message));
        when(event.getTimeMillis()).thenReturn(System.currentTimeMillis());
        when(event.getLevel()).thenReturn(level);
        when(event.getLoggerName()).thenReturn("TestLogger");
        when(event.getThreadName()).thenReturn(Thread.currentThread().getName());
        return event;
    }
}
