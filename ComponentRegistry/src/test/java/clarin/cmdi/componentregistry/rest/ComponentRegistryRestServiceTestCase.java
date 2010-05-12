package clarin.cmdi.componentregistry.rest;

import java.io.File;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.junit.After;
import org.junit.Before;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryImplTest;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.http.HTTPContainerFactory;

public abstract class ComponentRegistryRestServiceTestCase extends JerseyTest {
    //CommandLine test e.g.:  curl -i -H "Accept:application/json" -X GET  http://localhost:8080/ComponentRegistry/rest/registry/profiles

    protected static ComponentRegistry testRegistry;
    private static File registryDir;

    protected final static GenericType<List<ProfileDescription>> PROFILE_LIST_GENERICTYPE = new GenericType<List<ProfileDescription>>() {
    };
    protected final static GenericType<List<ComponentDescription>> COMPONENT_LIST_GENERICTYPE = new GenericType<List<ComponentDescription>>() {
    };

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new HTTPContainerFactory();
    }

    @Override
    protected AppDescriptor configure() {
        LowLevelAppDescriptor ad = new LowLevelAppDescriptor.Builder(ComponentRegistryRestService.class.getPackage().getName()).build();
        ResourceConfig resourceConfig = ad.getResourceConfig();
        resourceConfig.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, DummySecurityFilter.class.getName());
        return ad;
    }

    protected WebResource getResource() {
        return resource();
    }

    protected Builder getAuthenticatedResource(String path) {
        return getAuthenticatedResource(getResource().path(path));
    }

    protected Builder getAuthenticatedResource(WebResource resource) {
        return resource.header(HttpHeaders.AUTHORIZATION,
                "Basic " + new String(Base64.encode(DummyPrincipal.DUMMY_PRINCIPAL.getName() + ":dummy")));
    }

    @Before
    public void setUpTestRegistry() throws Exception {
        registryDir = ComponentRegistryImplTest.createTempRegistryDir();
        testRegistry = ComponentRegistryImplTest.getTestRegistry(registryDir);
    }

    protected void fillUp() throws Exception {
        RegistryTestHelper.addProfile(testRegistry, "profile1");
        RegistryTestHelper.addProfile(testRegistry, "profile2");
        RegistryTestHelper.addComponent(testRegistry, "component1");
        RegistryTestHelper.addComponent(testRegistry, "component2");
    }

    @After
    public void deleteAndRecreateEmptyRegistry() {
        ComponentRegistryImplTest.cleanUpRegistryDir(registryDir);
    }

}
