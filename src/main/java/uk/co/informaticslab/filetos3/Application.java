package uk.co.informaticslab.filetos3;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Application starter
 */
@SpringBootApplication
public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    /**
     * Used to convert bytes to megabytes in starting message
     */
    private static final int BYTES_TO_MB = 1048576;

    /**
     * A main method to start this application.
     */
    public static void main(String[] args) {
        logStartingMessages();
        DateTimeZone.setDefault(DateTimeZone.UTC);
        SpringApplication.run(Application.class, args);
    }

    private static void logStartingMessages() {
        LOG.debug(getDebugLogStartup());
        LOG.info(getInfoLogStartup());
    }

    private static String getInfoLogStartup() {
        Runtime runtime = Runtime.getRuntime();
        StringBuilder sb = new StringBuilder();
        sb.append("\n********************************************************");
        sb.append("\n* Starting Application...");
        sb.append("\n* Free Memory       : " + runtime.freeMemory() / BYTES_TO_MB + "MB");
        sb.append("\n* Total Memory      : " + runtime.totalMemory() / BYTES_TO_MB + "MB");
        sb.append("\n* Used Memory       : " + (runtime.totalMemory() - runtime.freeMemory()) / BYTES_TO_MB + "MB");
        sb.append("\n* Max Memory (-Xmx) : " + runtime.maxMemory() / BYTES_TO_MB + "MB");
        sb.append("\n********************************************************");
        sb.append("\n");
        return sb.toString();
    }

    private static String getDebugLogStartup() {
        Properties p = System.getProperties();
        Enumeration<Object> keys = p.keys();
        StringBuilder sb = new StringBuilder();
        sb.append("\n********************************************************");
        sb.append("\n* Current System Properties...");
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) p.get(key);
            sb.append("\n* " + key + ": " + value);
        }
        sb.append("\n********************************************************");
        sb.append("\n");
        return sb.toString();
    }
}

