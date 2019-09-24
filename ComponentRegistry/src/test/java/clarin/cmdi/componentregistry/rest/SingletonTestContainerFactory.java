package clarin.cmdi.componentregistry.rest;

import java.net.URI;

import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

/**
 * Factory for {@link JerseyTest}s that returns reusable servlet container
 * testing contexts thus reducing startup time
 *
 * @author george.georgovassilis@mpi.nl
 *
 */
public class SingletonTestContainerFactory implements TestContainerFactory {

    private TestContainerFactory targetFactory;
    private TestContainer testContainer;
    private boolean testContainerStarted = false;

    public void startTestContainer() {
        testContainer.start();
        testContainerStarted = true;
    }

    public void stopTestContainer() {
        testContainer.stop();
        testContainerStarted = false;
    }

    public boolean isTestContainerRunning() {
        return testContainerStarted;
    }

    public SingletonTestContainerFactory(TestContainerFactory factory) {
        this.targetFactory = factory;
    }

    @Override
    public Class<? extends AppDescriptor> supports() {
        return targetFactory.supports();
    }

    @Override
    public TestContainer create(URI baseUri, AppDescriptor ad)
            throws IllegalArgumentException {
        if (testContainer == null) {
            testContainer = targetFactory.create(baseUri, ad);
        }
        return testContainer;
    }

}
