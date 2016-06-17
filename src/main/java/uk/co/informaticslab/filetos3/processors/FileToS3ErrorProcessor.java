package uk.co.informaticslab.filetos3.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.informaticslab.filetos3.utils.Constants;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Handles an {@linkplain Exchange} that errors in the {@link uk.co.informaticslab.filetos3.routes.FileToS3Route}
 */
@Component
public class FileToS3ErrorProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(FileToS3ErrorProcessor.class);

    private final String errorDirectoryPath;

    /**
     * Constructor.
     * Reads in required properties from application.properties file.
     *
     * @param errorDirectoryPath directory path for error files to be written to
     */
    @Autowired
    public FileToS3ErrorProcessor(@Value("${errorDirectoryPath}") String errorDirectoryPath) {
        this.errorDirectoryPath = errorDirectoryPath;
    }

    /**
     * Writes an error file into {@link FileToS3ErrorProcessor#errorDirectoryPath}.
     * Error file has the same name as the file that has caused the error with an '.err' extension.
     * File contents will be each of the headers on the {@link Exchange}.
     *
     * @param exchange in-flight {@link Exchange}
     */
    public void process(Exchange exchange) {
        Map<String, Object> headers = exchange.getIn().getHeaders();
        writeErrorDetailFile(headers);
    }

    private void writeErrorDetailFile(Map<String, Object> headers) {
        String filename = (String) headers.get("CamelFileName");
        PrintWriter pw = null;
        try {
            String dt = Constants.DTF.print(DateTime.now());
            String errorDetailFilename = errorDirectoryPath + "/" + filename + "." + dt + ".err";
            LOG.debug("Writing error detail out to [{}]", errorDetailFilename);
            pw = new PrintWriter(new FileWriter(errorDetailFilename));
            pw.format("Error uploading file [%s] occurred at [%s]\n", filename, dt);
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                pw.format("Header [%s] :\n%s\n", header.getKey(), header.getValue());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            pw.flush();
            pw.close();
        }
    }

}
