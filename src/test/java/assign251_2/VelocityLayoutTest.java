package assign251_2;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VelocityLayoutTest {

    @Test
    void testVelocityLayoutFormatting() {
        // Create a simple log event
        LogEvent event = mock(LogEvent.class);
        when(event.getMessage()).thenReturn(new SimpleMessage("Hello, Velocity!"));
        when(event.getTimeMillis()).thenReturn(System.currentTimeMillis());
        when(event.getLevel()).thenReturn(org.apache.logging.log4j.Level.INFO);
        when(event.getLoggerName()).thenReturn("TestLogger");

        // Velocity template for testing
        String template = "Log Message: $message, Level: $level, Time: $timestamp, Logger: $loggerName";

        // Create a VelocityLayout with the test template
        VelocityLayout layout = VelocityLayout.createLayout(Charset.forName("UTF-8"), template);

        // Apply the layout and verify the output
        String formatted = layout.toSerializable(event);
        assertTrue(formatted.contains("Hello, Velocity!"));
        assertTrue(formatted.contains("Level: INFO"));
        assertTrue(formatted.contains("Logger: TestLogger"));
    }
}
