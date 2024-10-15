package assign251_2;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.layout.AbstractLayout;
import org.apache.logging.log4j.util.Strings;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Plugin(name = "VelocityLayout", category = "Core", elementType = Layout.ELEMENT_TYPE)
public class VelocityLayout extends AbstractLayout<String> {

    private final String template;
    private final Charset charset;  // Manually store the charset

    protected VelocityLayout(Charset charset, String template) {
        super(null, null);  // Passing null for header and footer
        this.template = template;
        this.charset = charset;  // Initialize the charset
    }

    @PluginFactory
    public static VelocityLayout createLayout(
            @PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset,
            @PluginElement("Template") String template) {
        return new VelocityLayout(charset, template);
    }

    @Override
    public String toSerializable(LogEvent event) {
        // Initialize Velocity if not already done
        Velocity.init();

        // Create a VelocityContext and populate it with log event data
        VelocityContext context = new VelocityContext();
        context.put("message", event.getMessage().getFormattedMessage());
        context.put("timestamp", event.getTimeMillis());
        context.put("level", event.getLevel().toString());
        context.put("loggerName", event.getLoggerName());

        // Generate the output based on the provided template
        StringWriter writer = new StringWriter();
        Velocity.evaluate(context, writer, "log", template);

        return writer.toString();
    }

    // Convert the log event to a byte array using the manually stored charset
    @Override
    public byte[] toByteArray(LogEvent event) {
        String serialized = toSerializable(event);
        return serialized.getBytes(charset);  // Use the manually stored charset
    }

    // Implement getContentType to return the MIME type of the layout
    @Override
    public String getContentType() {
        return "text/plain";  // You can change this to "text/html" if using HTML templates
    }

    @Override
    public byte[] getHeader() {
        return Strings.EMPTY.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] getFooter() {
        return Strings.EMPTY.getBytes(StandardCharsets.UTF_8);
    }
}
