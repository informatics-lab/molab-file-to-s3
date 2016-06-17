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
public class ErrorDirectoryHousekeepingProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorDirectoryHousekeepingRoute.class);

    private final Long errorFileExpireAfterMillis;
    private final File errorDirectory;

    /**
     * Constructor.
     * Reads in required properties from application.properties file.
     *
     * @param errorFileExpireAfterMillis millis after which files should be house-kept
     * @param errorDirectoryPath path of error file directory
     */
    @Autowired
    public ErrorDirectoryHousekeepingProcessor(@Value("${errorFileExpireAfterMillis}") Long errorFileExpireAfterMillis,
                                               @Value("${errorDirectoryPath}") String errorDirectoryPath) {
        this.errorFileExpireAfterMillis = errorFileExpireAfterMillis;
        this.errorDirectory = new File(errorDirectoryPath);
    }

    /**
     * Asses the {@link DateTime} timestamp of each of the error files.
     * If the timestamp is thought to have now expired, both the original data file and error file
     * will be removed from the filesystem.
     *
     * @param exchange in-flight {@link Exchange}
     */
    public void process(Exchange exchange) {

        for (File f : errorDirectory.listFiles()) {
            if(f.getName().endsWith(".err")) {

                String filename = f.getName();
                String[] filenameParts = filename.split("\\.");
                String dt = filenameParts[filenameParts.length - 2];
                DateTime errorDateTime = Constants.DTF.parseDateTime(dt);
                DateTime expiryDateTime = errorDateTime.plus(errorFileExpireAfterMillis);

                if (DateTime.now().isAfter(expiryDateTime)) {
                    expireFiles(f.getAbsolutePath());
                }

            }
        }

    }

    private void expireFiles(String absoluteFilePath) {
        String dataFilename = absoluteFilePath.substring(0, absoluteFilePath.length() - 25);
        File dataFile = new File(dataFilename);
        File errorDetailFile = new File(absoluteFilePath);
        LOG.info("Housekeeping expired files [{}, {}]", dataFile.getName(), errorDetailFile.getName());
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
