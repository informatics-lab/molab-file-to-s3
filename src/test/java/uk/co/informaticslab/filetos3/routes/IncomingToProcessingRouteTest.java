package uk.co.informaticslab.filetos3.routes;

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Random;

public class IncomingToProcessingRouteTest extends MolabCamelSpringTestSupport {

    private static final String MOCK_PROCESSING_DIR_ENDPOINT_URI = "mock:processing";

    private static final String FILENAME = "filename.test";

    @Rule
    public TemporaryFolder testIncomingDirectory = new TemporaryFolder();
    @Rule
    public TemporaryFolder testProcessingDirectory = new TemporaryFolder();

    private IncomingToProcessingRoute route;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context.stop();
        route = new IncomingToProcessingRoute(testIncomingDirectory.getRoot().getAbsolutePath(),
                testProcessingDirectory.getRoot().getAbsolutePath());
        context.addRoutes(route);
        testFileComponentConsumerPath();
        testFileComponentProducerPath();

        assertEquals("Test incoming directory empty", 0, testIncomingDirectory.getRoot().list().length);
        assertEquals("Test processing directory empty", 0, testProcessingDirectory.getRoot().list().length);
    }

    @Test
    public void configure() throws Exception {

        context.getRouteDefinitions().get(0).adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById(IncomingToProcessingRoute.PROCESSING_DIR_ENDPOINT_ID).after().to(MOCK_PROCESSING_DIR_ENDPOINT_URI);
            }
        });

        context.start();

        int contentLength = 20;
        byte[] fileContent = new byte[contentLength];
        new Random().nextBytes(fileContent);
        File f = writeFileToTemporaryFolder(testIncomingDirectory, fileContent, FILENAME);

        MockEndpoint mockProcessingDirEndpoint = getMockEndpoint(MOCK_PROCESSING_DIR_ENDPOINT_URI);
        mockProcessingDirEndpoint.expectedMessageCount(1);

        assertMockEndpointsSatisfied();

        assertEquals("Test processing directory file exists", 1, testProcessingDirectory.getRoot().list().length);

    }

    public void testFileComponentConsumerPath() throws Exception {
        String actual = route.getFileComponentConsumerPath();
        String expected = "file://" + testIncomingDirectory.getRoot().getAbsolutePath() + "?initialDelay=1000&readLock=fileLock&delete=true";
        assertEquals("Camel file component consumer paths", expected, actual);
    }

    public void testFileComponentProducerPath() throws Exception {
        String actual = route.getFileComponentProducerPath();
        String expected = "file://" + testProcessingDirectory.getRoot().getAbsolutePath();
        assertEquals("Camel file component producer paths", expected, actual);
    }

}