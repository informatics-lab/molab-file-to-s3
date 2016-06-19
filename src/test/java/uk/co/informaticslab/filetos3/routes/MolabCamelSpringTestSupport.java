package uk.co.informaticslab.filetos3.routes;

import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileOutputStream;


public abstract class MolabCamelSpringTestSupport extends CamelSpringTestSupport {

    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext(getRouteExcludingApplicationContext());
    }

    public File writeFileToTemporaryFolder(TemporaryFolder folder, byte[] content, String filename) throws Exception {
        File f = folder.newFile(filename);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(content);
        fos.flush();
        fos.close();
        return f;
    }
}
