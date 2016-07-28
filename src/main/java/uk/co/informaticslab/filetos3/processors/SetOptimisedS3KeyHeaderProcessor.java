package uk.co.informaticslab.filetos3.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adds the optimised S3 key to the {@linkplain Exchange}
 */
@Component
public class SetOptimisedS3KeyHeaderProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(SetOptimisedS3KeyHeaderProcessor.class);

    private static final String CAMEL_FILE_NAME = "CamelFileName";
    private static final String CAMEL_AWS_S3_KEY = "CamelAwsS3Key";

    public void process(Exchange exchange) {
        String fileName = exchange.getIn().getHeader(CAMEL_FILE_NAME, String.class);
        exchange.getIn().setHeader(CAMEL_AWS_S3_KEY, getOptimisedS3Key(fileName));
    }

    /**
     * Appends 4 random characters as a directory to the file path.
     *
     * @param fileName current file name
     * @return random 4 character directory path prepended file name
     * @see <a href="https://aws.amazon.com/blogs/aws/amazon-s3-performance-tips-tricks-seattle-hiring-event/">S3 performance optimisation</a>
     */
    public String getOptimisedS3Key(String fileName) {
        UUID randomUUID = UUID.randomUUID();
        StringBuilder sb = new StringBuilder();
        sb.append(randomUUID.toString().substring(0, 4));
        sb.append("/");
        sb.append(fileName);
        LOG.debug("Optimised S3 key [{}] ", sb);
        return sb.toString();
    }

}
