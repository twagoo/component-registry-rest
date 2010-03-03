package clarin.cmdi.componentregistry.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.ParseException;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryImplTest;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.http.HTTPContainerFactory;

public class ComponentRegistryRestServiceTest extends JerseyTest {

    //CommandLine test e.g.:  curl -i -H "Accept:application/json" -X GET  http://localhost:8080/ComponentRegistry/rest/registry/profiles

    private static ComponentRegistry testRegistry;
    private static File registryDir;

    private final static GenericType<List<ProfileDescription>> PROFILE_LIST_GENERICTYPE = new GenericType<List<ProfileDescription>>() {
    };
    private final static GenericType<List<ComponentDescription>> COMPONENT_LIST_GENERICTYPE = new GenericType<List<ComponentDescription>>() {
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

    private WebResource getResource() {
        return resource();
    }

    private Builder getAuthenticatedResource(String path) {
        return getResource().path(path).header(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encode(DummyPrincipal.DUMMY_PRINCIPAL.getName()+":dummy")));
    }

    @Test
    public void testGetRegisteredProfiles() throws Exception {
        List<ProfileDescription> response = getResource().path("/registry/profiles").accept(MediaType.APPLICATION_XML).get(
                PROFILE_LIST_GENERICTYPE);
        assertEquals(2, response.size());
        response = getResource().path("/registry/profiles").accept(MediaType.APPLICATION_JSON).get(PROFILE_LIST_GENERICTYPE);
        assertEquals(2, response.size());
    }

    @Test
    public void testGetRegisteredComponents() {
        List<ComponentDescription> response = getResource().path("/registry/components").accept(MediaType.APPLICATION_XML).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, response.size());
        response = getResource().path("/registry/components").accept(MediaType.APPLICATION_JSON).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, response.size());
    }

    @Test
    public void testGetRegisteredComponent() {
        CMDComponentSpec component = getResource().path("/registry/components/clarin.eu:cr1:component1").accept(MediaType.APPLICATION_JSON)
                .get(CMDComponentSpec.class);
        assertNotNull(component);
        assertEquals("Access", component.getCMDComponent().get(0).getName());
        component = getResource().path("/registry/components/clarin.eu:cr1:component2").accept(MediaType.APPLICATION_XML).get(
                CMDComponentSpec.class);
        assertNotNull(component);
        assertEquals("Access", component.getCMDComponent().get(0).getName());

        assertEquals("clarin.eu:cr1:component2", component.getHeader().getID());
        assertEquals("testComponent", component.getHeader().getName());
        assertEquals("Test Description", component.getHeader().getDescription());
    }

    @Test
    public void testDeleteRegisteredComponent() throws Exception {
        List<ComponentDescription> components = getResource().path("/registry/components").get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, components.size());
        CMDComponentSpec profile = getResource().path("/registry/components/clarin.eu:cr1:component1").get(CMDComponentSpec.class);
        assertNotNull(profile);
        ClientResponse response = getAuthenticatedResource("/registry/components/clarin.eu:cr1:component1").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        components = getResource().path("/registry/components").get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(1, components.size());

        response = getAuthenticatedResource("/registry/components/clarin.eu:cr1:component2").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        components = getResource().path("/registry/components").get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(0, components.size());

        response = getAuthenticatedResource("/registry/components/clarin.eu:cr1:component1").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetRegisteredProfile() throws Exception {
        CMDComponentSpec profile = getResource().path("/registry/profiles/clarin.eu:cr1:profile1").accept(MediaType.APPLICATION_JSON).get(
                CMDComponentSpec.class);
        assertNotNull(profile);
        assertEquals("Actor", profile.getCMDComponent().get(0).getName());
        profile = getResource().path("/registry/profiles/clarin.eu:cr1:profile2").accept(MediaType.APPLICATION_XML).get(
                CMDComponentSpec.class);
        assertNotNull(profile);
        assertEquals("Actor", profile.getCMDComponent().get(0).getName());

        assertEquals("clarin.eu:cr1:profile2", profile.getHeader().getID());
        assertEquals("testProfile", profile.getHeader().getName());
        assertEquals("Test Description", profile.getHeader().getDescription());

        try {
            profile = getResource().path("/registry/profiles/clarin.eu:cr1:profileXXXX").accept(MediaType.APPLICATION_XML).get(
                    CMDComponentSpec.class);
            fail("Exception should have been thrown resouce does not exist, HttpStatusCode 204");
        } catch (UniformInterfaceException e) {
            assertEquals(204, e.getResponse().getStatus());
        }
    }

    @Test
    public void testGetRegisteredProfileRawData() throws Exception {
        String profile = getResource().path("/registry/profiles/clarin.eu:cr1:profile1/xsd").accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema"));
        assertTrue(profile.endsWith("</xs:schema>"));

        profile = getResource().path("/registry/profiles/clarin.eu:cr1:profile1/xml").accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<CMD_ComponentSpec"));
        assertTrue(profile.endsWith("</CMD_ComponentSpec>\n"));

        try {
            getResource().path("/registry/components/clarin.eu:cr1:component1/xsl").accept(MediaType.TEXT_XML).get(String.class);
            fail("Should have thrown exception, unsopported path parameter");
        } catch (UniformInterfaceException e) {//server error
        }
    }

    @Test
    public void testDeleteRegisteredProfile() throws Exception {
        List<ProfileDescription> profiles = getResource().path("/registry/profiles").get(PROFILE_LIST_GENERICTYPE);
        assertEquals(2, profiles.size());
        CMDComponentSpec profile = getResource().path("/registry/profiles/clarin.eu:cr1:profile1").get(CMDComponentSpec.class);
        assertNotNull(profile);

        ClientResponse response = getAuthenticatedResource("/registry/profiles/clarin.eu:cr1:profile1").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        profiles = getResource().path("/registry/profiles").get(PROFILE_LIST_GENERICTYPE);
        assertEquals(1, profiles.size());

        response = getAuthenticatedResource("/registry/profiles/clarin.eu:cr1:profile2").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        profiles = getResource().path("/registry/profiles").get(PROFILE_LIST_GENERICTYPE);
        assertEquals(0, profiles.size());

        response = getAuthenticatedResource("/registry/profiles/clarin.eu:cr1:profile1").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetRegisteredComponentRawData() throws Exception {
        String component = getResource().path("/registry/components/clarin.eu:cr1:component1/xsd").accept(MediaType.TEXT_XML).get(
                String.class);
        assertTrue(component.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema"));
        assertTrue(component.endsWith("</xs:schema>"));

        component = getResource().path("/registry/components/clarin.eu:cr1:component1/xml").accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(component.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<CMD_ComponentSpec"));
        assertTrue(component.endsWith("</CMD_ComponentSpec>\n"));

        try {
            getResource().path("/registry/components/clarin.eu:cr1:component1/jpg").accept(MediaType.TEXT_XML).get(String.class);
            fail("Should have thrown exception, unsopported path parameter");
        } catch (UniformInterfaceException e) {
        }
    }

    @Test
    public void testRegisterProfile() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, TestHelper.getTestProfileContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "ProfileTest1");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, "My Test Profile");
        RegisterResponse response = getAuthenticatedResource("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertTrue(response.isProfile());
        ProfileDescription profileDesc = (ProfileDescription) response.getDescription();
        assertNotNull(profileDesc);
        assertEquals("ProfileTest1", profileDesc.getName());
        assertEquals("My Test Profile", profileDesc.getDescription());
        assertEquals("J.Unit", profileDesc.getCreatorName());
        assertTrue(profileDesc.getId().startsWith(ComponentRegistry.REGISTRY_ID + "p_"));
        assertNotNull(profileDesc.getRegistrationDate());
        assertEquals("http://localhost:9998/registry/profiles/" + profileDesc.getId(), profileDesc.getHref());
    }

    @Test
    public void testRegisterComponent() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, TestHelper.getComponentTestContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "ComponentTest1");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, "My Test Component");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "TestGroup");
        RegisterResponse response = getAuthenticatedResource("/registry/components").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        ComponentDescription desc = (ComponentDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("ComponentTest1", desc.getName());
        assertEquals("My Test Component", desc.getDescription());
        assertEquals("J.Unit", desc.getCreatorName());
        assertEquals("TestGroup", desc.getGroupName());
        assertTrue(desc.getId().startsWith(ComponentRegistry.REGISTRY_ID + "c_"));
        assertNotNull(desc.getRegistrationDate());
        String url = getResource().getUriBuilder().build().toString();
        assertEquals(url + "registry/components/" + desc.getId(), desc.getHref());
    }

    @Test
    public void testRegisterProfileInvalidData() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        String notAValidProfile = "<CMD_ComponentSpec> </CMD_ComponentSpec>";
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, new ByteArrayInputStream(notAValidProfile.getBytes()),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "ProfileTest1");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, "My Test Profile");
        RegisterResponse postResponse = getAuthenticatedResource("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertTrue(postResponse.isProfile());
        assertFalse(postResponse.isRegistered());
        assertEquals(1, postResponse.getErrors().size());
        assertTrue(postResponse.getErrors().get(0).contains("SAXParseException"));
    }

    @Test
    public void testRegisterProfileInvalidDescriptionAndContent() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<CMD_ComponentSpec> \n"; //No isProfile attribute
        profileContent += "    <Header />\n";
        profileContent += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
        profileContent += "        <AttributeList>\n";
        profileContent += "            <Attribute>\n";
        profileContent += "                <Name>Name</Name>\n";
        profileContent += "                <Type>string</Type>\n";
        profileContent += "            </Attribute>\n";
        profileContent += "        </AttributeList>\n";
        profileContent += "    </CMD_Component>\n";
        profileContent += "</CMD_ComponentSpec>\n";
        form.field("data", new ByteArrayInputStream(profileContent.getBytes()), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field("name", "");//Empty name so invalid
        form.field("description", "My Test Profile");
        RegisterResponse response = getAuthenticatedResource("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(response.isRegistered());
        assertEquals(2, response.getErrors().size());
        assertNotNull(response.getErrors().get(0));
        assertEquals(MDValidator.ISPROFILE_NOT_SET_ERROR, response.getErrors().get(1));
    }

    @Test
    public void testRegisterComponentAsProfile() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, TestHelper.getComponentTestContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "t");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, "My Test");
        RegisterResponse response = getAuthenticatedResource("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(response.isRegistered());
        assertTrue(response.isProfile());
        assertEquals(1, response.getErrors().size());
        assertEquals(MDValidator.MISMATCH_ERROR, response.getErrors().get(0));
    }

    @Before
    public void setUpTestRegistry() throws ParseException, JAXBException {
        registryDir = ComponentRegistryImplTest.createTempRegistryDir();
        testRegistry = ComponentRegistryImplTest.getTestRegistry(registryDir);
        TestHelper.addProfile(testRegistry, ComponentRegistry.REGISTRY_ID + "profile1");
        TestHelper.addProfile(testRegistry, ComponentRegistry.REGISTRY_ID + "profile2");
        TestHelper.addComponent(testRegistry, ComponentRegistry.REGISTRY_ID + "component1");
        TestHelper.addComponent(testRegistry, ComponentRegistry.REGISTRY_ID + "component2");
    }

    @After
    public void deleteAndRecreateEmptyRegistry() {
        ComponentRegistryImplTest.cleanUpRegistryDir(registryDir);
    }

}
