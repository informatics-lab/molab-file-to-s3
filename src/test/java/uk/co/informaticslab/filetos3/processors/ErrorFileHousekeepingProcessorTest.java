package uk.co.informaticslab.filetos3.processors;

import mockit.Mocked;
import org.apache.camel.Exchange;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;


public class ErrorFileHousekeepingProcessorTest {

    private static final String MOCK_DATE_TIME_STRING = "2016-01-01T00:00:00Z";
    private static final String TEST_FILENAME = "filename.test";

    @Rule
    public TemporaryFolder testErrorDirectory = new TemporaryFolder();

    private ErrorDirectoryHousekeepingProcessor processor;
    private File errorDetailFile;

    @Before
    public void setUp() throws Exception {
        assertEquals("Test error directory empty", 0, testErrorDirectory.getRoot().list().length);
        processor = new ErrorDirectoryHousekeepingProcessor(1000L, testErrorDirectory.getRoot().getAbsolutePath());
    }

    @Test
    public void testProcess(@Mocked final Exchange mockExchange) throws Exception {

        testErrorDirectory.newFile(TEST_FILENAME);
        errorDetailFile = testErrorDirectory.newFile(TEST_FILENAME + "." + MOCK_DATE_TIME_STRING + ".err");

        assertEquals("Test error files exist", 2, testErrorDirectory.getRoot().list().length);

        processor.process(mockExchange);

        assertEquals("Test error files removed", 0, testErrorDirectory.getRoot().list().length);

    }

}