package clarin.cmdi.componentregistry.rest;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.UserDao;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.multipart.impl.FormDataMultiPartDispatchProvider;
import com.sun.jersey.spi.container.servlet.WebComponent;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

/**
 * Base test that starts a servlet container with the component registry
 * @author george.georgovassilis@mpi.nl
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
	"classpath:spring-config/applicationContext.xml",
	"classpath:spring-config/datasource-hsqldb.xml" })
@TransactionConfiguration(defaultRollback = true)
@Transactional
public abstract class ComponentRegistryRestServiceTestCase extends JerseyTest {
    // CommandLine test e.g.: curl -i -H "Accept:application/json" -X GET
    // http://localhost:8080/ComponentRegistry/rest/registry/profiles

    protected final static GenericType<List<ProfileDescription>> PROFILE_LIST_GENERICTYPE = new GenericType<List<ProfileDescription>>() {
    };
    protected final static GenericType<List<ComponentDescription>> COMPONENT_LIST_GENERICTYPE = new GenericType<List<ComponentDescription>>() {
    };
    protected final static GenericType<List<Comment>> COMMENT_LIST_GENERICTYPE = new GenericType<List<Comment>>() {
    };

    private static SingletonTestContainerFactory _testContainerFactory;

    @Override
    public void setUp() throws Exception {
	if (!_testContainerFactory.isTestContainerRunning()) {
	    _testContainerFactory.startTestContainer();
	}
    }

    @Override
    public void tearDown() throws Exception {
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
	if (_testContainerFactory == null) {
	    _testContainerFactory = new SingletonTestContainerFactory(
		    super.getTestContainerFactory());
	}
	;
	return _testContainerFactory;
    }

    @Autowired
    private UserDao userDao;

    protected String getApplicationContextFile() {
	// sorry for the duplication, but JerseyTest is not aware of
	// @ContextConfiguration
	return "classpath:spring-config/applicationContext.xml, classpath:spring-config/datasource-hsqldb.xml";
    }

    @Override
    protected AppDescriptor configure() {
	WebAppDescriptor.Builder builder = new WebAppDescriptor.Builder()
		.contextParam("contextConfigLocation",
			getApplicationContextFile())
		.servletClass(SpringServlet.class)
		.initParam(WebComponent.RESOURCE_CONFIG_CLASS,
			ClassNamesResourceConfig.class.getName())
		.initParam(
			ClassNamesResourceConfig.PROPERTY_CLASSNAMES,
			FormDataMultiPartDispatchProvider.class.getName() + ";"
				+ ComponentRegistryRestService.class.getName())
		.addFilter(DummySecurityFilter.class, "DummySecurityFilter")
		.requestListenerClass(RequestContextListener.class)
		.contextListenerClass(ContextLoaderListener.class);
	return builder.build();
    }

    protected WebResource getResource() {
	return resource();
    }

    protected Builder getAuthenticatedResource(String path) {
	return getAuthenticatedResource(getResource().path(path));
    }

    protected Builder getAuthenticatedResource(WebResource resource) {
	return resource.header(
		HttpHeaders.AUTHORIZATION,
		"Basic "
			+ new String(Base64
				.encode(DummyPrincipal.DUMMY_PRINCIPAL
					.getName() + ":dummy")));
    }

    protected void createUserRecord() {
	RegistryUser user = new RegistryUser();
	user.setName("Database test user");
	user.setPrincipalName(DummyPrincipal.DUMMY_PRINCIPAL.getName());
	userDao.insertUser(user);
    }

    protected UserDao getUserDao() {
	return userDao;
    }

}
