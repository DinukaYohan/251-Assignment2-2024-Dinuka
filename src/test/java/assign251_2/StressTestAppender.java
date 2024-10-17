package assign251_2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class StressTestAppender {

    private Logger logger;

    @BeforeEach
    void setUp() {
        // Reset the logger and configuration before each test
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.reconfigure();
        logger = LogManager.getLogger("StressTestLogger");
    }

    @Test
    void stressTestMemAppenderArrayList() throws InterruptedException {
        int maxSize = 1000;
        MemAppender.resetInstance();
        MemAppender appender = MemAppender.getInstance("MemAppenderArrayList", null, null, maxSize, new ArrayList<>());
        appender.setLayout(PatternLayout.newBuilder().withPattern("%m%n").build());
        appender.start();

        attachAppender(appender);

        performLogging("MemAppender with ArrayList", 5000);

        // Allow time for logs to be processed
        Thread.sleep(1000);

        appender.stop();
    }

    @Test
    void stressTestMemAppenderLinkedList() throws InterruptedException {
        int maxSize = 1000;
        MemAppender.resetInstance();
        MemAppender appender = MemAppender.getInstance("MemAppenderLinkedList", null, null, maxSize, new LinkedList<>());
        appender.setLayout(PatternLayout.newBuilder().withPattern("%m%n").build());
        appender.start();

        attachAppender(appender);

        performLogging("MemAppender with LinkedList", 5000);

        // Allow time for logs to be processed
        Thread.sleep(1000);

        appender.stop();
    }

    @Test
    void stressTestConsoleAppender() throws InterruptedException {
        ConsoleAppender appender = ConsoleAppender.newBuilder()
                .setName("ConsoleAppender")
                .setLayout(PatternLayout.newBuilder().withPattern("%m%n").build())
                .build();
        appender.start();

        attachAppender(appender);

        performLogging("ConsoleAppender", 5000);

        // Allow time for logs to be processed
        Thread.sleep(1000);

        appender.stop();
    }

    @Test
    void stressTestFileAppender() throws InterruptedException {
        FileAppender appender = FileAppender.newBuilder()
                .setName("FileAppender")
                .withFileName("logs/stress-test.log")
                .setLayout(PatternLayout.newBuilder().withPattern("%m%n").build())
                .build();
        appender.start();

        attachAppender(appender);

        performLogging("FileAppender", 5000);

        // Allow time for logs to be processed
        Thread.sleep(1000);

        appender.stop();
    }

    @ParameterizedTest
    @ValueSource(ints = {1000, 5000, 10000})
    void stressTestMemAppenderWithDifferentMaxSizes(int maxSize) throws InterruptedException {
        MemAppender.resetInstance();
        MemAppender appender = MemAppender.getInstance("MemAppenderVariableSize", null, null, maxSize, new ArrayList<>());
        appender.setLayout(PatternLayout.newBuilder().withPattern("%m%n").build());
        appender.start();

        attachAppender(appender);

        performLogging("MemAppender with maxSize " + maxSize, maxSize * 2);

        // Allow time for logs to be processed
        Thread.sleep(1000);

        appender.stop();
    }

    @Test
    void comparePatternLayoutAndVelocityLayout() throws InterruptedException {
        // Test with PatternLayout
        MemAppender.resetInstance();
        MemAppender appenderPattern = MemAppender.getInstance("MemAppenderPattern", null, null, 5000, new ArrayList<>());
        appenderPattern.setLayout(PatternLayout.newBuilder().withPattern("%m%n").build());
        appenderPattern.start();

        attachAppender(appenderPattern);

        performLogging("MemAppender with PatternLayout", 5000);

        // Allow time for logs to be processed
        Thread.sleep(1000);

        appenderPattern.stop();

        // Test with VelocityLayout
        MemAppender.resetInstance();
        MemAppender appenderVelocity = MemAppender.getInstance("MemAppenderVelocity", null, null, 5000, new ArrayList<>());
        appenderVelocity.setLayout(VelocityLayout.createLayout(StandardCharsets.UTF_8, "$m$n"));
        appenderVelocity.start();

        attachAppender(appenderVelocity);

        performLogging("MemAppender with VelocityLayout", 5000);

        // Allow time for logs to be processed
        Thread.sleep(1000);

        appenderVelocity.stop();
    }

    private void attachAppender(Appender appender) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());

        loggerConfig.addAppender(appender, Level.DEBUG, null);
        loggerConfig.setLevel(Level.DEBUG);

        context.updateLoggers();
    }

    private void performLogging(String testName, int numberOfLogs) {
        System.out.println("Starting " + testName + " with " + numberOfLogs + " log messages.");

        long startTime = System.nanoTime();

        for (int i = 0; i < numberOfLogs; i++) {
            logger.debug("Log message number " + i);
        }

        long endTime = System.nanoTime();
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        System.out.println("Completed " + testName + " in " + durationMillis + " ms.");
    }
}
