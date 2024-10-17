package assign251_2;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Plugin(name = "MemAppender", category = "Core", elementType = "appender", printObject = true)
public class MemAppender extends AbstractAppender {

    private static MemAppender instance;
    private final int maxSize;
    private final List<LogEvent> logEvents;
    private long discardedLogCount;
    private volatile Layout<? extends Serializable> customLayout;

    // Private constructor to enforce singleton pattern
    private MemAppender(String name, Layout<? extends Serializable> layout, Filter filter, int maxSize, List<LogEvent> logEvents) {
        super(name, filter, null, true);

        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be greater than zero");
        }

        this.customLayout = layout;
        this.maxSize = maxSize;
        this.logEvents = (logEvents != null) ? logEvents : new ArrayList<>();
        this.discardedLogCount = 0;
    }

    // Static factory method to create or retrieve the singleton instance
    @PluginFactory
    public static synchronized MemAppender getInstance(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") Filter filter,
            @PluginAttribute("maxSize") int maxSize) {

        if (instance == null) {
            instance = new MemAppender(name != null ? name : "MemAppender", layout, filter, maxSize, null);
        }
        return instance;
    }

    // Overloaded method for dependency injection
    public static synchronized MemAppender getInstance(String name, Layout<? extends Serializable> layout, Filter filter, int maxSize, List<LogEvent> logEvents) {
        if (instance == null) {
            instance = new MemAppender(name != null ? name : "MemAppender", layout, filter, maxSize, logEvents);
        }
        return instance;
    }

    // Method to reset the singleton instance (useful for testing)
    public static void resetInstance() {
        instance = null;
    }

    @Override
    public void append(LogEvent event) {
        if (event == null) {
            throw new NullPointerException("LogEvent cannot be null");
        }

        if (logEvents.size() >= maxSize) {
            logEvents.remove(0);  // Remove the oldest log event
            discardedLogCount++;
        }
        logEvents.add(event.toImmutable());  // Store an immutable copy of the event
    }

    public List<LogEvent> getCurrentLogs() {
        return Collections.unmodifiableList(new ArrayList<>(logEvents));
    }

    public long getDiscardedLogCount() {
        return discardedLogCount;
    }

    public void setLayout(Layout<? extends Serializable> layout) {
        this.customLayout = layout;
    }

    public Layout<? extends Serializable> getLayout() {
        return this.customLayout;
    }

    public List<String> getEventStrings() {
        if (this.customLayout == null) {
            throw new IllegalStateException("Layout is not set.");
        }

        List<String> eventStrings = new ArrayList<>();
        Charset charset = StandardCharsets.UTF_8;  // Default charset

        if (this.customLayout instanceof AbstractStringLayout) {
            charset = ((AbstractStringLayout) this.customLayout).getCharset();
        }

        for (LogEvent event : logEvents) {
            byte[] byteArray = this.customLayout.toByteArray(event);
            String logMessage = new String(byteArray, charset);
            eventStrings.add(logMessage);
        }
        return Collections.unmodifiableList(eventStrings);
    }

    public void printLogs() {
        if (this.customLayout == null) {
            throw new IllegalStateException("Layout is not set.");
        }

        Charset charset = StandardCharsets.UTF_8;  // Default charset

        if (this.customLayout instanceof AbstractStringLayout) {
            charset = ((AbstractStringLayout) this.customLayout).getCharset();
        }

        for (LogEvent event : logEvents) {
            byte[] byteArray = this.customLayout.toByteArray(event);
            String logMessage = new String(byteArray, charset);
            System.out.print(logMessage);
        }
        logEvents.clear();
    }

    // Reset the internal state (for finer control)
    public void reset() {
        logEvents.clear();
        discardedLogCount = 0;
    }
}
