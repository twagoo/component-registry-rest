package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.impl.database.ComponentRegistryDatabase;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.USERSPACE_PARAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import java.util.Collections;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class ConcurrentRestServiceTest extends ComponentRegistryRestServiceTestCase {

    private final static Logger LOG = LoggerFactory.getLogger(ConcurrentRestServiceTest.class);
    private int NR_OF_PROFILES = 50;
    private int NR_OF_COMPONENTS = 50;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
	ComponentRegistryDatabase.resetAndCreateAllTables(jdbcTemplate);
	createUserRecord();
    }

    @Override
    protected String getApplicationContextFile() {
	return "classpath:applicationContext.xml";
    }
    
    @Test
    public void testConcurrentRegisterProfile() throws Exception {
	List<String> errors = new ArrayList();
	List<Thread> ts = new ArrayList<Thread>();

	registerProfiles(ts, NR_OF_PROFILES, errors, "false");
	registerProfiles(ts, NR_OF_PROFILES, errors, "true");

	registerComponents(ts, NR_OF_COMPONENTS, errors, "false");
	registerComponents(ts, NR_OF_COMPONENTS, errors, "true");
	runAllThreads(ts);
	if (errors.size() > 0) {
	    System.out.println(Arrays.toString(errors.toArray()));
	    fail();
	}
	assertProfiles(NR_OF_PROFILES, "false");
	assertProfiles(NR_OF_PROFILES, "true");

	assertComponents(NR_OF_COMPONENTS, "false");
	assertComponents(NR_OF_COMPONENTS, "true");
    }

    private void assertProfiles(int nrOfProfiles, String userSpace) {
	List<ProfileDescription> response = getAuthenticatedResource(
		getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, userSpace)).
		accept(MediaType.APPLICATION_XML).get(
		PROFILE_LIST_GENERICTYPE);
	Collections.sort(response, descriptionComparator);
	assertEquals("half should be deleted", nrOfProfiles / 2, response.size());
	for (int i = 0; i < nrOfProfiles / 2; i++) {
	    ProfileDescription desc = response.get(i);
	    assertEquals("Test Profile" + (i * 2 + 1000), desc.getName());
	    assertEquals("Test Profile" + (i * 2 + 1000) + " Description", desc.getDescription());
	}
    }
    private Comparator<AbstractDescription> descriptionComparator = new Comparator<AbstractDescription>() {

	@Override
	public int compare(AbstractDescription o1, AbstractDescription o2) {
	    return o1.getName().compareTo(o2.getName());
	}
    };

    private void assertComponents(int nrOfComponents, String userSpace) {
	List<ComponentDescription> cResponse = getAuthenticatedResource(
		getResource().path("/registry/components").queryParam(USERSPACE_PARAM, userSpace)).
		accept(MediaType.APPLICATION_XML).get(
		COMPONENT_LIST_GENERICTYPE);
	Collections.sort(cResponse, descriptionComparator);
	assertEquals("half should be deleted", nrOfComponents / 2, cResponse.size());
	for (int i = 0; i < nrOfComponents / 2; i++) {
	    ComponentDescription desc = cResponse.get(i);
	    assertEquals("Test Component" + (i * 2 + 1000), desc.getName());
	    assertEquals("Test Component" + (i * 2 + 1000) + " Description", desc.getDescription());
	}
    }

    private void runAllThreads(List<Thread> ts) throws InterruptedException {
	for (Thread thread : ts) {
	    thread.start();
	    thread.join(10);
	}
	for (Thread thread : ts) {
	    thread.join(); //Wait till all are finished
	}
    }

    private void registerProfiles(List<Thread> ts, int size, final List<String> errors, String userSpace) throws InterruptedException {
	for (int i = 0; i < size; i++) {
	    final boolean shouldDelete = (i % 2) == 1;
	    Thread thread = createThread("/registry/profiles/", userSpace, "Test Profile" + (i + 1000), shouldDelete, RegistryTestHelper.getTestProfileContent(), errors);
	    ts.add(thread);
	}
    }

    private void registerComponents(List<Thread> ts, int size, final List<String> errors, String userSpace) throws InterruptedException {
	for (int i = 0; i < size; i++) {
	    final boolean shouldDelete = (i % 2) == 1;
	    Thread thread = createThread("/registry/components/", userSpace, "Test Component" + (i + 1000), shouldDelete,
		    RegistryTestHelper.getComponentTestContent(), errors);
	    ts.add(thread);
	}
    }

    private Thread createThread(final String path, final String userSpace, final String name, final boolean alsoDelete,
	    InputStream content, final List<String> errors) throws InterruptedException {
	final FormDataMultiPart form = new FormDataMultiPart();
	form.field(ComponentRegistryRestService.DATA_FORM_FIELD, content, MediaType.APPLICATION_OCTET_STREAM_TYPE);
	form.field(ComponentRegistryRestService.NAME_FORM_FIELD, name);
	form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, name + " Description");
	Thread t = new Thread(new Runnable() {

	    @Override
	    public void run() {
		//                System.out.println("THREAD STARTED"+Thread.currentThread().getName());
		RegisterResponse registerResponse = getAuthenticatedResource(
			getResource().path(path).queryParam(USERSPACE_PARAM, userSpace)).
			type(MediaType.MULTIPART_FORM_DATA).post(
			RegisterResponse.class, form);
		if (!registerResponse.isRegistered()) {
		    errors.add("Failed to register " + Arrays.toString(registerResponse.getErrors().toArray()));
		}
		LOG.debug(">>>>>>>>>>>>>>>> [Thread " + hashCode() + "] REGISTERING DESCRIPTION " + name + " " + registerResponse.getDescription().getId() + (Boolean.valueOf(userSpace) ? " userspace" : "") + (alsoDelete ? " alsoDelete" : ""));
		if (alsoDelete) {
		    LOG.debug(">>>>>>>>>>>>>>>> [Thread " + hashCode() + "] DELETING DESCRIPTION " + name + " " + registerResponse.getDescription().getId() + (Boolean.valueOf(userSpace) ? " userspace " : "") + (alsoDelete ? " alsoDelete" : ""));
		    ClientResponse response = getAuthenticatedResource(
			    getResource().path(path + registerResponse.getDescription().getId()).queryParam(USERSPACE_PARAM, userSpace)).
			    delete(ClientResponse.class);
		    if (response.getStatus() != 200) {
			errors.add("Failed to delete " + registerResponse.getDescription());
		    }
		}
		//                System.out.println("THREAD FINISHED"+Thread.currentThread().getName());
	    }
	});
	return t;

    }
}
