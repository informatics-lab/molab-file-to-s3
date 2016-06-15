package uk.co.informaticslab.filetos3.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@link org.apache.camel.Route} for sending files to AWS-S3.
 */
@Component
public class FileToS3Route extends RouteBuilder{

    private static final Logger LOG = LoggerFactory.getLogger(FileToS3Route.class);
    private static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
    private static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";

    private final String fromDirectoryPath;
    private final String toS3ParentBucketName;
    private final String toAWSAccessKey;
    private final String toAWSSecretKey;

    /**
     * Constructor.
     * Reads in required properties from environment variables and application.properties file.
     * @param fromDirectoryPath directory path to scrape for files
     * @param toS3ParentBucketName parent directory to send data to
     */
    @Autowired
    public FileToS3Route(@Value("${fromDirectoryPath}") String fromDirectoryPath,
                         @Value("${toS3ParentBucketName}") String toS3ParentBucketName) {
        this.fromDirectoryPath = fromDirectoryPath;
        this.toS3ParentBucketName = toS3ParentBucketName;
        this.toAWSAccessKey = getFromSystemEnvironmentVariables(AWS_ACCESS_KEY_ID);
        this.toAWSSecretKey = getFromSystemEnvironmentVariables(AWS_SECRET_ACCESS_KEY);
    }

    private static String getFromSystemEnvironmentVariables(String environmentVariable) {
        String variable = System.getenv(environmentVariable);
        if(variable == null || variable.equals("")) {
            LOG.error("No valid environment variable set for {}",environmentVariable);
            throw new RuntimeException(String.format("No valid environment variable set for %s", environmentVariable));
        }
        return variable;
    }

    /**
     * {@inheritDoc}
     */
    public void configure() {
        from(getFileComponentConsumerPath())
                .setHeader("S3Destination", simple(getS3Destination()))
                .log(LoggingLevel.INFO, "Uploading " + simple("${header.CamelFileName}") + " to AWS-S3 bucket " + simple("${header.S3Destination}"))
                .recipientList(getS3ComponentProducerPath(simple("${header.S3Destination}").getText()));
    }

    /**
     * Gets the Camel File Component path for the consumer endpoint.
     * @see <a href="http://camel.apache.org/file2.html">Camel File Component Documentation</a>
     * @return Camel File Component consumer uri
     */
    public String getFileComponentConsumerPath() {
        StringBuilder sb = new StringBuilder("file://");
        sb.append(fromDirectoryPath);
        sb.append("?");
        sb.append("initialDelay=1000");
        sb.append("&");
        sb.append("delete=true");
        LOG.debug("File component consumer uri [{}]",sb);
        return sb.toString();
    }

    /**
     * Gets the Camel AWS-S3 Component path for the producer endpoint.
     * @see <a href="http://camel.apache.org/aws-s3.html">Camel AWS-S3 Component Documentation</a>
     * @param s3Bucket the S3 bucket destination path
     * @return Camel AWS-S3 Component producer uri
     */
    public String getS3ComponentProducerPath(String s3Bucket) {
        StringBuilder sb = new StringBuilder("aws-s3://");
        sb.append(s3Bucket);
        sb.append("?");
        sb.append("accessKey=");
        sb.append(toAWSAccessKey);
        sb.append("&");
        sb.append("secretKey=");
        sb.append(toAWSSecretKey);
        LOG.debug("S3 component producer uri [{}]",sb);
        return sb.toString();
    }

    /**
     * Appends the current year and month to the {@link FileToS3Route#toS3ParentBucketName} as child directories.
     * @return AWS-S3 destination path in the format {@link FileToS3Route#toS3ParentBucketName}/YYYY/MMM
     */
    public String getS3Destination() {
        DateTime now = DateTime.now();
        StringBuilder sb = new StringBuilder(toS3ParentBucketName);
        sb.append("/");
        sb.append(now.year().toString());
        sb.append("/");
        sb.append(now.monthOfYear().toString());
        LOG.debug("S3 destination [{}] ",sb);
        return sb.toString();
    }

}