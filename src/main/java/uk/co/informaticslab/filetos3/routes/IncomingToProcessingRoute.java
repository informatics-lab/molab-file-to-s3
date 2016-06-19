package uk.co.informaticslab.filetos3.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * {@link org.apache.camel.Route} for ingesting files into the system.
 */
@Component
public class IncomingToProcessingRoute extends RouteBuilder {

    public static final String PROCESSING_DIR_ENDPOINT_ID = UUID.randomUUID().toString();

    private static final Logger LOG = LoggerFactory.getLogger(IncomingToProcessingRoute.class);

    private final String incomingDirectoryPath;
    private final String processingDirectoryPath;

    /**
     * Constructor.
     * Reads in required properties from application.properties file.
     *
     * @param incomingDirectoryPath path of incoming data directory
     * @param processingDirectoryPath path of processing data directory
     */
    @Autowired
    public IncomingToProcessingRoute(@Value("${incomingDirectoryPath}") String incomingDirectoryPath,
                                     @Value("${processingDirectoryPath}") String processingDirectoryPath) {
        this.incomingDirectoryPath = incomingDirectoryPath;
        this.processingDirectoryPath = processingDirectoryPath;
    }

    /**
     * {@inheritDoc}
     */
    public void configure() {
        from(getFileComponentConsumerPath())
                .routeId(this.getClass().getSimpleName())
                .log(LoggingLevel.INFO, LOG, "Moving file [${header.CamelFileName}] from ["+incomingDirectoryPath+"] to ["+processingDirectoryPath+"]")
                .to(getFileComponentProducerPath()).id(PROCESSING_DIR_ENDPOINT_ID);
    }

    /**
     * Gets the Camel File Component path for the consumer endpoint.
     * @see <a href="http://camel.apache.org/file2.html">Camel File Component Documentation</a>
     * @return Camel File Component consumer uri
     */
    public String getFileComponentConsumerPath() {
        StringBuilder sb = new StringBuilder("file://");
        sb.append(incomingDirectoryPath);
        sb.append("?");
        sb.append("initialDelay=1000");
        sb.append("&");
        sb.append("readLock=fileLock");
        sb.append("&");
        sb.append("delete=true");
        LOG.debug("File component consumer uri [{}]", sb);
        return sb.toString();
    }

    /**
     * Gets the Camel File Component path for the producer endpoint.
     * @see <a href="http://camel.apache.org/file2.html">Camel File Component Documentation</a>
     * @return Camel File Component producer uri
     */
    public String getFileComponentProducerPath() {
        StringBuilder sb = new StringBuilder("file://");
        sb.append(processingDirectoryPath);
        LOG.debug("File component producer uri [{}]", sb);
        return sb.toString();
    }

}
