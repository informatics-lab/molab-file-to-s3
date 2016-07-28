package uk.co.informaticslab.filetos3.processors;

import mockit.Expectations;
import mockit.Mocked;
import org.apache.camel.Exchange;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FileToS3ErrorProcessorTest {

    private static final DateTime MOCK_NOW = new DateTime("2016-01-01T00:00:00Z");

    @Rule
    public TemporaryFolder testErrorDirectory = new TemporaryFolder();

    private FileToS3ErrorProcessor processor;

    @Before
    public void setUp() {

        new Expectations(DateTime.class) {{
            DateTime.now();
            result = MOCK_NOW;
        }};

        processor = new FileToS3ErrorProcessor(testErrorDirectory.getRoot().getAbsolutePath());
    }

    @Test
    public void testProcess(@Mocked final Exchange mockExchange) {

        final Map<String, Object> testHeaders = new HashMap<String, Object>();
        testHeaders.put("CamelFileName", "filename.test");
        testHeaders.put("key1", "value1");
        testHeaders.put("key2", "value2");
        testHeaders.put("key3", "value3");

        new Expectations() {{
            mockExchange.getIn().getHeaders();
            result = testHeaders;
        }};

        processor.process(mockExchange);

        assertEquals("Error detail file exists", true,
                new File(testErrorDirectory.getRoot().getAbsolutePath() + "/filename.test.2016-01-01T00:00:00Z.err").exists());
        //TODO assert file contents are as expected

    }

}