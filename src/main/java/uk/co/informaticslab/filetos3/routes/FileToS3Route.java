package uk.co.informaticslab.filetos3.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.informaticslab.filetos3.processors.FileToS3ErrorProcessor;

import java.util.UUID;

/**
 * {@link org.apache.camel.Route} for uploading files to AWS-S3.
 */
@Component
public class FileToS3Route extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(FileToS3Route.class);

    public static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
    public static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";
    public static final String UPLOAD_TO_S3_ENDPOINT_ID = UUID.randomUUID().toString();
    public static final String ERROR_ENDPOINT_ID = UUID.randomUUID().toString();
    public static final String ERROR_ENDPOINT_PROCESSOR_ID = UUID.randomUUID().toString();

    private final FileToS3ErrorProcessor fileToS3ErrorProcessor;
    private final String fromDirectoryPath;
    private final String errorDirectoryPath;
    private final String toS3BucketName;
    private final String toAWSAccessKey;
    private final String toAWSSecretKey;

    /**
     * Constructor.
     * Reads in required properties from environment variables and application.properties file.
     * @param fromDirectoryPath directory path to poll for files
     * @param toS3BucketName AWS-S3 bucket to upload data to
     * @param fileToS3ErrorProcessor processor for dealing with failed {@linkplain Exchange}s
     */
    @Autowired
    public FileToS3Route(@Value("${fromDirectoryPath}") String fromDirectoryPath,
                         @Value("${errorDirectoryPath}") String errorDirectoryPath,
                         @Value("${toS3BucketName}") String toS3BucketName,
                         FileToS3ErrorProcessor fileToS3ErrorProcessor) {
        this.fromDirectoryPath = fromDirectoryPath;
        this.errorDirectoryPath = errorDirectoryPath;
        this.toS3BucketName = toS3BucketName;
        this.fileToS3ErrorProcessor = fileToS3ErrorProcessor;
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
                .routeId(this.getClass().getSimpleName())
                .onException(Exception.class)
                    .maximumRedeliveries(2)
                    .handled(true)
                    .useOriginalMessage()
                    .log(LoggingLevel.WARN, LOG, "File [${header.CamelFileName}] is being moved to the error directory [${header.ErrorDirectory}]")
                    .process(fileToS3ErrorProcessor).id(ERROR_ENDPOINT_PROCESSOR_ID)
                    .to(getFileComponentErrorProducerPath()).id(ERROR_ENDPOINT_ID)
                    .end()
                .setHeader("CamelAwsS3Key", simple(getDateTimeFileName("${header.CamelFileName}")))
                .setHeader("CamelAwsS3ContentLength", header("CamelFileLength"))
                .setHeader("S3Bucket", simple(toS3BucketName))
                .setHeader("ErrorDirectory", simple(errorDirectoryPath))
                .log(LoggingLevel.DEBUG, LOG, "Current headers : [${headers}]")
                .log(LoggingLevel.INFO, LOG, "Uploading [${header.CamelFileName}] to AWS-S3 bucket [${header.S3Bucket}/${header.CamelAwsS3Key}]")
                .to(getS3ComponentProducerPath()).id(UPLOAD_TO_S3_ENDPOINT_ID)
                .log(LoggingLevel.INFO, LOG, "[${header.CamelFileName}] upload complete");
    }

    /**
     * Gets the Camel File Component path for the error producer endpoint.
     * @see <a href="http://camel.apache.org/file2.html">Camel File Component Documentation</a>
     * @return Camel File Component consumer uri
     */
    public String getFileComponentErrorProducerPath() {
        StringBuilder sb = new StringBuilder("file://");
        sb.append(errorDirectoryPath);
        LOG.debug("Error producer uri [{}]", sb);
        return sb.toString();
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
        LOG.debug("File component consumer uri [{}]", sb);
        return sb.toString();
    }

    /**
     * Gets the Camel AWS-S3 Component path for the producer endpoint.
     * @see <a href="http://camel.apache.org/aws-s3.html">Camel AWS-S3 Component Documentation</a>
     * @return Camel AWS-S3 Component producer uri
     */
    public String getS3ComponentProducerPath() {
        StringBuilder sb = new StringBuilder("aws-s3://");
        sb.append(toS3BucketName);
        sb.append("?");
        sb.append("accessKey=");
        sb.append(toAWSAccessKey);
        sb.append("&");
        sb.append("secretKey=");
        sb.append(toAWSSecretKey);
        sb.append("&");
        sb.append("region=eu-west-1");
        LOG.debug("S3 component producer uri [{}]", sb);
        return sb.toString();
    }

    /**
     * Appends the current year and month as parent directories to the filename.
     * @return  date time prepended filename in the format YYYY/MM/filename
     */
    public String getDateTimeFileName(String filename) {
        DateTime now = DateTime.now();
        StringBuilder sb = new StringBuilder();
        sb.append(now.year().get());
        sb.append("/");
        sb.append(now.monthOfYear().get());
        sb.append("/");
        sb.append(filename);
        LOG.debug("Date time file name [{}] ", sb);
        return sb.toString();
    }

}