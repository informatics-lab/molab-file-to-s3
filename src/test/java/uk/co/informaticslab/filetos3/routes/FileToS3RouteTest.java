package uk.co.informaticslab.filetos3.routes;

import mockit.Expectations;
import mockit.Mocked;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.mock.MockEndpoint;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.co.informaticslab.filetos3.processors.FileToS3ErrorProcessor;

import java.io.File;
import java.util.Random;

public class FileToS3RouteTest extends MolabCamelSpringTestSupport {

    private static final String MOCK_UPLOAD_TO_S3_ENDPOINT_URI = "mock:s3";
    private static final String MOCK_ERROR_PROCESSOR_URI = "mock:error.processor";
    private static final String MOCK_ERROR_ENDPOINT_URI = "mock:error";
    private static final String TO_BUCKET = "to";
    private static final String FILENAME = "filename.test";
    private static final String MOCK_ACCESS_KEY_ID = "mock-aws-access-key-id";
    private static final String MOCK_SECRET_ACCESS_KEY_ID = "mock-aws-secret-access-key";
    private static final DateTime MOCK_NOW = new DateTime("2016-01-01T00:00:00Z");

    @Rule
    public TemporaryFolder testProcessingDirectory = new TemporaryFolder();

    @Rule
    public TemporaryFolder testErrorDirectory = new TemporaryFolder();

    private FileToS3Route route;

    @Mocked
    private FileToS3ErrorProcessor processor;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context.stop();
        new Expectations(System.class, DateTime.class) {{
            System.getenv(FileToS3Route.AWS_ACCESS_KEY_ID);
            result = MOCK_ACCESS_KEY_ID;
            System.getenv(FileToS3Route.AWS_SECRET_ACCESS_KEY);
            result = MOCK_SECRET_ACCESS_KEY_ID;
            DateTime.now();
            result = MOCK_NOW;
        }};
        route = new FileToS3Route(testProcessingDirectory.getRoot().getAbsolutePath(),
                testErrorDirectory.getRoot().getAbsolutePath() + "/",
                TO_BUCKET,
                processor);
        context.addRoutes(route);
        testFileComponentConsumerPath();
        testS3ComponentProducerPath();
    }

    @Test
    public void testConfig() throws Exception {

        context.getRouteDefinitions().get(0).adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById(FileToS3Route.UPLOAD_TO_S3_ENDPOINT_ID).replace().to(MOCK_UPLOAD_TO_S3_ENDPOINT_URI);
            }
        });
        context.start();

        int contentLength = 20;
        byte[] fileContent = new byte[contentLength];
        new Random().nextBytes(fileContent);
        File f = writeFileToTemporaryFolder(testProcessingDirectory, fileContent, FILENAME);


        MockEndpoint mockS3Endpoint = getMockEndpoint(MOCK_UPLOAD_TO_S3_ENDPOINT_URI);

        mockS3Endpoint.expectedMessageCount(1);
        mockS3Endpoint.expectedHeaderReceived("CamelAwsS3Key", "2016/1/" + FILENAME);
        mockS3Endpoint.expectedHeaderReceived("CamelAwsS3ContentLength", contentLength);

        assertMockEndpointsSatisfied();


        assertEquals("File contents", f, mockS3Endpoint.getExchanges().get(0).getIn().getBody(GenericFile.class).getFile());
    }

    @Test
    public void testErrorConfig() throws Exception {

        context.getRouteDefinitions().get(0).adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById(FileToS3Route.UPLOAD_TO_S3_ENDPOINT_ID).replace().to(MOCK_UPLOAD_TO_S3_ENDPOINT_URI);
                weaveById(FileToS3Route.ERROR_ENDPOINT_PROCESSOR_ID).replace().to(MOCK_ERROR_PROCESSOR_URI);
                weaveById(FileToS3Route.ERROR_ENDPOINT_ID).after().to(MOCK_ERROR_ENDPOINT_URI);
            }
        });

        MockEndpoint mockS3Endpoint = getMockEndpoint(MOCK_UPLOAD_TO_S3_ENDPOINT_URI);
        MockEndpoint mockErrorProcessor = getMockEndpoint(MOCK_ERROR_PROCESSOR_URI);
        MockEndpoint mockErrorEndpont = getMockEndpoint(MOCK_ERROR_ENDPOINT_URI);


        mockS3Endpoint.whenAnyExchangeReceived(new Processor() {
            public void process(Exchange exchange) throws Exception {
                throw new Exception("error");
            }
        });

        context.start();

        int contentLength = 20;
        byte[] fileContent = new byte[contentLength];
        new Random().nextBytes(fileContent);
        File f = writeFileToTemporaryFolder(testProcessingDirectory, fileContent, FILENAME);

        mockS3Endpoint.expectedMessageCount(3);
        mockErrorProcessor.expectedMessageCount(1);
        mockErrorEndpont.expectedMessageCount(1);

        assertMockEndpointsSatisfied();

        System.out.println(mockErrorProcessor.getExchanges().get(0).getIn().getHeaders());

        assertEquals("Error data file exists", 1, testErrorDirectory.getRoot().list().length);

    }

    public void testFileComponentConsumerPath() throws Exception {
        String actual = route.getFileComponentConsumerPath();
        String expected = "file://" + testProcessingDirectory.getRoot().getAbsolutePath() + "?initialDelay=1000&delete=true";
        assertEquals("Camel file component consumer paths", expected, actual);
    }

    public void testS3ComponentProducerPath() throws Exception {
        String actual = route.getS3ComponentProducerPath();
        String expected = "aws-s3://" + TO_BUCKET + "?accessKey=" + MOCK_ACCESS_KEY_ID + "&secretKey=" + MOCK_SECRET_ACCESS_KEY_ID + "&region=eu-west-1";
        assertEquals("Camel AWS-S3 component producer paths", expected, actual);
    }
}