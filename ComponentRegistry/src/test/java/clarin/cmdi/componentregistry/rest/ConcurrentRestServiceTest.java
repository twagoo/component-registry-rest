package clarin.cmdi.componentregistry.rest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

import com.sun.jersey.multipart.FormDataMultiPart;

public class ConcurrentRestServiceTest extends ComponentRegistryRestServiceTestCase {

    @Test
    public void testConcurrentRegisterProfile() throws Exception {
        List<FormDataMultiPart> forms = new ArrayList<FormDataMultiPart>();
        int nrOfProfiles = 100;
        for (int i = 0; i < nrOfProfiles; i++) {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.DATA_FORM_FIELD, RegistryTestHelper.getTestProfileContent(),
                    MediaType.APPLICATION_OCTET_STREAM_TYPE);
            form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "ProfileTest" + (i + 1000));
            form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, "My Test Profile" + (i + 1000));
            forms.add(form);
        }
        List<Thread> ts = new ArrayList<Thread>();
        for (final FormDataMultiPart form : forms) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    getAuthenticatedResource("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
                }
            });
            t.start();
            t.join(5);
            ts.add(t);
        }
        for (Thread thread : ts) {
            thread.join();
        }
        List<ProfileDescription> response = getResource().path("/registry/profiles").accept(MediaType.APPLICATION_XML).get(
                PROFILE_LIST_GENERICTYPE);
        assertEquals(nrOfProfiles, response.size());
        for (int i = 0; i < nrOfProfiles; i++) {
            ProfileDescription desc = response.get(i);
            assertEquals("ProfileTest" + (i + 1000), desc.getName());
            assertEquals("My Test Profile" + (i + 1000), desc.getDescription());
        }
    }
    //TODO PD: add components and delete similarly everything at the end.
}
