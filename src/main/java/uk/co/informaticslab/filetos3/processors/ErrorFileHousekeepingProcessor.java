package uk.co.informaticslab.filetos3.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.informaticslab.filetos3.routes.ErrorDirectoryHousekeepingRoute;
import uk.co.informaticslab.filetos3.utils.Constants;

import java.io.File;

/**
 * {@link Processor} for housekeeping error files
 */
@Component
public class ErrorFileHousekeepingProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorDirectoryHousekeepingRoute.class);

    private final Long errorFileExpireAfterMillis;

    /**
     * Constructor.
     * Reads in required properties from application.properties file.
     *
     * @param errorFileExpireAfterMillis millis after which files should be house-kept
     */
    @Autowired
    public ErrorFileHousekeepingProcessor(@Value("${errorFileExpireAfterMillis}") Long errorFileExpireAfterMillis) {
        this.errorFileExpireAfterMillis = errorFileExpireAfterMillis;
    }

    /**
     * Asses the {@link DateTime} timestamp of the error file.
     * If the timestamp is thought to have now expired both the original data file and error file
     * will be removed from the filesystem.
     *
     * @param exchange in-flight {@link Exchange}
     */
    public void process(Exchange exchange) {
        String filename = exchange.getIn().getHeader("CamelFileName", String.class);
        String[] filenameParts = filename.split(".");
        DateTime errorDateTime = Constants.DTF.parseDateTime(filenameParts[filenameParts.length - 2]);
        DateTime expiryDateTime = errorDateTime.plus(errorFileExpireAfterMillis);

        if (DateTime.now().isAfter(expiryDateTime)) {
            expireFiles(exchange.getIn().getHeader("CamelFileAbsolutePath", String.class));
        }
    }

    private void expireFiles(String absoluteFilePath) {
        String dataFilename = absoluteFilePath.substring(0, absoluteFilePath.length() - 25);
        File dataFile = new File(dataFilename);
        File errorDetailFile = new File(absoluteFilePath);
        LOG.info("Housekeeping expired files [{},{}]", dataFile.getName(), errorDetailFile.getName());
        deleteFile(dataFile);
        deleteFile(errorDetailFile);
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            LOG.debug("Deleting file [{}]", file.getName());
            file.delete();
        }
    }
}
