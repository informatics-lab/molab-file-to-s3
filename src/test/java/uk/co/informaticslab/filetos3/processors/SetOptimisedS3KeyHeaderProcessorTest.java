package uk.co.informaticslab.filetos3.processors;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * Created by tom on 28/07/2016.
 */
public class SetOptimisedS3KeyHeaderProcessorTest {

    private SetOptimisedS3KeyHeaderProcessor processor;

    @Before
    public void setUp() {
        processor = new SetOptimisedS3KeyHeaderProcessor();
    }

    @Test
    public void testProcess(@Mocked final CamelContext mockContext) {

        Exchange exchange1 = new DefaultExchange(mockContext);
        Exchange exchange2 = new DefaultExchange(mockContext);

        String fileName1 = "filename1.test";
        String fileName2 = "filename2.test";

        exchange1.getIn().setHeader("CamelFileName", fileName1);
        exchange2.getIn().setHeader("CamelFileName", fileName2);

        new Expectations(UUID.class) {};

        processor.process(exchange1);
        processor.process(exchange2);

        new Verifications() {{
            UUID.randomUUID();
            times = 2;
        }};

        assertTrue("S3 key header added", exchange1.getIn().getHeader("CamelAwsS3Key", String.class).contains(fileName1));
        assertTrue("S3 key header added", exchange2.getIn().getHeader("CamelAwsS3Key", String.class).contains(fileName2));
    }

}