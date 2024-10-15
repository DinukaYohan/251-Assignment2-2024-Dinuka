package assign251_2;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Plugin(name = "MemAppender", category = "Core", elementType = Appender.ELEMENT_TYPE)
public class MemAppender extends AbstractAppender {

    private static MemAppender instance;
    private final List<LogEvent> logEvents;
    private final int maxSize;
    private long discardedLogCount;

    protected MemAppender(String name, Layout<? extends Serializable> layout, Filter filter, int maxSize) {
        super(name, filter, layout, true);
        this.maxSize = maxSize;
        this.logEvents = new ArrayList<>();
        this.discardedLogCount = 0;
        System.out.println("Max size is set to: " + maxSize);
    }

    @PluginFactory
    public static MemAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("maxSize") int maxSize) {
        if (instance == null) {
            instance = new MemAppender(name, layout, filter, maxSize);
        }
        return instance;
    }

    // Reset method to clear the singleton instance
    public static void resetInstance() {
        instance = null;
    }

    @Override
    public void append(LogEvent event) {
        System.out.println("Appending event. Current size: " + logEvents.size());
        if (logEvents.size() >= maxSize) {
            logEvents.remove(0);  // Remove the oldest log
            discardedLogCount++;
            System.out.println("Log discarded. Discarded count: " + discardedLogCount);
        }
        logEvents.add(event);
        System.out.println("Event added. Total logs: " + logEvents.size());
    }

    public List<LogEvent> getCurrentLogs() {
        return Collections.unmodifiableList(logEvents);
    }

    public long getDiscardedLogCount() {
        return discardedLogCount;
    }
}
