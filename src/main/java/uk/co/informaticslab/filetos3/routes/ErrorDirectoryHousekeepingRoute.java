package uk.co.informaticslab.filetos3.routes;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.co.informaticslab.filetos3.processors.ErrorFileHousekeepingProcessor;

/**
 * {@link org.apache.camel.Route} for housekeeping the error directory
 */
public class ErrorDirectoryHousekeepingRoute extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorDirectoryHousekeepingRoute.class);

    private final ErrorFileHousekeepingProcessor errorDirectoryHousekeepingProcessor;
    private final String errorDirectoryPath;

    /**
     * Constructor.
     * Reads in required properties from application.properties file.
     *
     * @param errorDirectoryPath directory path to poll for files
     * @param errorDirectoryHousekeepingProcessor processor for housekeeping error files
     */
    @Autowired
    public ErrorDirectoryHousekeepingRoute(@Value("${errorDirectoryPath}") String errorDirectoryPath,
                                           ErrorFileHousekeepingProcessor errorDirectoryHousekeepingProcessor) {
        this.errorDirectoryPath = errorDirectoryPath;
        this.errorDirectoryHousekeepingProcessor = errorDirectoryHousekeepingProcessor;
    }

    /**
     * {@inheritDoc}
     */
    public void configure() {
        from(getFileComponentConsumerPath())
                .process(errorDirectoryHousekeepingProcessor);
    }

    /**
     * Gets the Camel File Component path for the consumer endpoint.
     * @see <a href="http://camel.apache.org/file2.html">Camel File Component Documentation</a>
     * @return Camel File Component consumer uri
     */
    public String getFileComponentConsumerPath() {
        StringBuilder sb = new StringBuilder("file://");
        sb.append(errorDirectoryPath);
        sb.append("?");
        sb.append("delay=21600000");
        sb.append("&");
        sb.append("include=*/.err");
        LOG.debug("File component consumer uri [{}]", sb);
        return sb.toString();
    }

}
