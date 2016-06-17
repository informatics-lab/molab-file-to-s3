package uk.co.informaticslab.filetos3.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.informaticslab.filetos3.processors.ErrorDirectoryHousekeepingProcessor;

/**
 * {@link org.apache.camel.Route} for housekeeping the error directory
 */
public class ErrorDirectoryHousekeepingRoute extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorDirectoryHousekeepingRoute.class);

    private final ErrorDirectoryHousekeepingProcessor errorDirectoryHousekeepingProcessor;

    /**
     * Constructor.
     *
     * @param errorDirectoryHousekeepingProcessor processor for housekeeping error files
     */
    @Autowired
    public ErrorDirectoryHousekeepingRoute(
            ErrorDirectoryHousekeepingProcessor errorDirectoryHousekeepingProcessor) {
        this.errorDirectoryHousekeepingProcessor = errorDirectoryHousekeepingProcessor;
    }

    /**
     * {@inheritDoc}
     */
    public void configure() {
        from(getTimerComponentPath())
                .log(LoggingLevel.INFO, LOG, "Running housekeeping on the error directory")
                .process(errorDirectoryHousekeepingProcessor);
    }

    /**
     * Gets the Camel Timer Component path.
     * @see <a href="http://camel.apache.org/timer.html">Camel Timer Component Documentation</a>
     * @return Camel Timer Component uri
     */
    public String getTimerComponentPath() {
        StringBuilder sb = new StringBuilder("timer:ErrorDirectoryHousekeeping");
        sb.append("?");
        sb.append("period=21600000");
        LOG.debug("Timer component uri [{}]", sb);
        return sb.toString();
    }

}
