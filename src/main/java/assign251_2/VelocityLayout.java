package assign251_2;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.AbstractLayout;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.nio.charset.Charset;

@Plugin(name = "VelocityLayout", category = "Core", elementType = Layout.ELEMENT_TYPE, printObject = true)
public class VelocityLayout extends AbstractLayout<String> {

    private String pattern;
    private final Charset charset;

    protected VelocityLayout(Charset charset, String pattern) {
        super(null, null);

        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty.");
        }

        this.pattern = pattern;
        this.charset = charset;
    }

    @PluginFactory
    public static VelocityLayout createLayout(
            @PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset,
            @PluginAttribute("pattern") String pattern) {
        return new VelocityLayout(charset, pattern);
    }

    public void setPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty.");
        }
        this.pattern = pattern;
    }

    @Override
    public String toSerializable(LogEvent event) {
        if (event == null) {
            throw new NullPointerException("LogEvent cannot be null");
        }

        Velocity.init();

        // Create a VelocityContext and populate it with log event data
        VelocityContext context = new VelocityContext();
        context.put("c", event.getLoggerName());  // Category
        context.put("d", new java.util.Date(event.getTimeMillis()).toString());  // Date

        String message = (event.getMessage() != null) ? event.getMessage().getFormattedMessage() : "null";
        context.put("m", message);  // Message

        context.put("p", event.getLevel().toString());  // Priority
        context.put("t", event.getThreadName());  // Thread
        context.put("n", System.lineSeparator());  // Line separator

        // Generate the output based on the provided pattern
        StringWriter writer = new StringWriter();
        Velocity.evaluate(context, writer, "log", pattern);

        return writer.toString();
    }

    @Override
    public byte[] toByteArray(LogEvent event) {
        String serialized = toSerializable(event);
        return serialized.getBytes(charset);
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }
}
