package clarin.cmdi.componentregistry.rest;

import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.USERSPACE_PARAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.multipart.FormDataMultiPart;

public class ComponentRegistryRestServiceTest extends ComponentRegistryRestServiceTestCase {

    @Test
    public void testGetRegisteredProfiles() throws Exception {
        fillUp();
        List<ProfileDescription> response = getResource().path("/registry/profiles").accept(MediaType.APPLICATION_XML).get(
                PROFILE_LIST_GENERICTYPE);
        assertEquals(2, response.size());
        response = getResource().path("/registry/profiles").accept(MediaType.APPLICATION_JSON).get(PROFILE_LIST_GENERICTYPE);
        assertEquals(2, response.size());
    }

    @Test
    public void testGetRegisteredComponents() throws Exception {
        fillUp();
        List<ComponentDescription> response = getResource().path("/registry/components").accept(MediaType.APPLICATION_XML).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, response.size());
        response = getResource().path("/registry/components").accept(MediaType.APPLICATION_JSON).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, response.size());
    }

    @Test
    public void testGetUserComponents() throws Exception {
        fillUp();
        List<ComponentDescription> response = getAuthenticatedResource(
                getResource().path("/registry/components").queryParam(USERSPACE_PARAM, "true")).accept(MediaType.APPLICATION_XML).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(0, response.size());
        response = getAuthenticatedResource(getResource().path("/registry/components")).accept(MediaType.APPLICATION_JSON).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals("Get public components", 2, response.size());
        ClientResponse cResponse = getResource().path("/registry/components").queryParam(USERSPACE_PARAM, "true").accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals("Trying to get userspace without credentials", 500, cResponse.getStatus());
    }

    @Test
    public void testGetRegisteredComponent() throws Exception {
        fillUp();
        CMDComponentSpec component = getResource().path("/registry/components/clarin.eu:cr1:component1").accept(MediaType.APPLICATION_JSON)
                .get(CMDComponentSpec.class);
        assertNotNull(component);
        assertEquals("Access", component.getCMDComponent().get(0).getName());
        component = getResource().path("/registry/components/clarin.eu:cr1:component2").accept(MediaType.APPLICATION_XML).get(
                CMDComponentSpec.class);
        assertNotNull(component);
        assertEquals("Access", component.getCMDComponent().get(0).getName());

        assertEquals("clarin.eu:cr1:component2", component.getHeader().getID());
        assertEquals("component2", component.getHeader().getName());
        assertEquals("Test Description", component.getHeader().getDescription());
    }

    @Test
    public void testDeleteRegisteredComponent() throws Exception {
        fillUp();
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
    public void testDeleteRegisteredComponentStillUsed() throws Exception {
        String content = "";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"XXX\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
        content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ComponentDescription compDesc1 = RegistryTestHelper.addComponent(testRegistry, "XXX1", content);

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ComponentDescription compDesc2 = RegistryTestHelper.addComponent(testRegistry, "YYY1", content);

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"ZZZ\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ProfileDescription profile = RegistryTestHelper.addProfile(testRegistry, "TestProfile3", content);

        List<ComponentDescription> components = getResource().path("/registry/components").get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, components.size());

        ClientResponse response = getAuthenticatedResource("/registry/components/" + compDesc1.getId()).delete(ClientResponse.class);
        assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals(
                "Still used by the following profiles: \n - TestProfile3\nStill used by the following components: \n - YYY1\nTry to change above mentioned references first.",
                response.getEntity(String.class));

        components = getResource().path("/registry/components").get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, components.size());

        response = getAuthenticatedResource("/registry/profiles/" + profile.getId()).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
        response = getAuthenticatedResource("/registry/components/" + compDesc2.getId()).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
        response = getAuthenticatedResource("/registry/components/" + compDesc1.getId()).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
        components = getResource().path("/registry/components").get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(0, components.size());
    }

    @Test
    public void testGetRegisteredProfile() throws Exception {
        fillUp();
        CMDComponentSpec profile = getResource().path("/registry/profiles/clarin.eu:cr1:profile1").accept(MediaType.APPLICATION_JSON).get(
                CMDComponentSpec.class);
        assertNotNull(profile);
        assertEquals("Actor", profile.getCMDComponent().get(0).getName());
        profile = getResource().path("/registry/profiles/clarin.eu:cr1:profile2").accept(MediaType.APPLICATION_XML).get(
                CMDComponentSpec.class);
        assertNotNull(profile);
        assertEquals("Actor", profile.getCMDComponent().get(0).getName());

        assertEquals("clarin.eu:cr1:profile2", profile.getHeader().getID());
        assertEquals("profile2", profile.getHeader().getName());
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
        fillUp();
        String profile = getResource().path("/registry/profiles/clarin.eu:cr1:profile1/xsd").accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema"));
        assertTrue(profile.endsWith("</xs:schema>"));

        profile = getResource().path("/registry/profiles/clarin.eu:cr1:profile1/xml").accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<CMD_ComponentSpec"));
        assertTrue(profile.endsWith("</CMD_ComponentSpec>\n"));
        assertTrue(profile.contains("xsi:schemaLocation"));

        try {
            getResource().path("/registry/components/clarin.eu:cr1:component1/xsl").accept(MediaType.TEXT_XML).get(String.class);
            fail("Should have thrown exception, unsupported path parameter");
        } catch (UniformInterfaceException e) {//server error
        }
    }

    @Test
    public void testDeleteRegisteredProfile() throws Exception {
        fillUp();
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
        fillUp();
        String component = getResource().path("/registry/components/clarin.eu:cr1:component1/xsd").accept(MediaType.TEXT_XML).get(
                String.class);
        assertTrue(component.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema"));
        assertTrue(component.endsWith("</xs:schema>"));

        component = getResource().path("/registry/components/clarin.eu:cr1:component1/xml").accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(component.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<CMD_ComponentSpec"));
        assertTrue(component.endsWith("</CMD_ComponentSpec>\n"));
        assertTrue(component.contains("xsi:schemaLocation"));

        try {
            getResource().path("/registry/components/clarin.eu:cr1:component1/jpg").accept(MediaType.TEXT_XML).get(String.class);
            fail("Should have thrown exception, unsopported path parameter");
        } catch (UniformInterfaceException e) {
        }
    }

    @Test
    public void testRegisterProfile() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, RegistryTestHelper.getTestProfileContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "ProfileTest1");
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD, "TestDomain");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, "My Test Profile");
        RegisterResponse response = getAuthenticatedResource("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertTrue(response.isProfile());
        assertFalse(response.isInUserSpace());
        ProfileDescription profileDesc = (ProfileDescription) response.getDescription();
        assertNotNull(profileDesc);
        assertEquals("ProfileTest1", profileDesc.getName());
        assertEquals("My Test Profile", profileDesc.getDescription());
        assertEquals("TestDomain", profileDesc.getDomainName());
        assertEquals("J.Unit", profileDesc.getCreatorName());
        assertTrue(profileDesc.getId().startsWith(ComponentRegistry.REGISTRY_ID + "p_"));
        assertNotNull(profileDesc.getRegistrationDate());
        assertEquals("http://localhost:9998/registry/profiles/" + profileDesc.getId(), profileDesc.getHref());
    }

    @Test
    public void testRegisterUserspaceProfile() throws Exception {
        List<ProfileDescription> profiles = getAuthenticatedResource(
                getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, "true")).accept(MediaType.APPLICATION_XML).get(
                PROFILE_LIST_GENERICTYPE);
        assertEquals("user registered profiles", 0, profiles.size());
        assertEquals("public registered profiles", 0, getAuthenticatedResource("/registry/profiles").accept(MediaType.APPLICATION_XML).get(
                PROFILE_LIST_GENERICTYPE).size());
        FormDataMultiPart form = createFormData(RegistryTestHelper.getTestProfileContent());
        RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, "true"))
                .type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        assertTrue(response.isProfile());
        assertTrue(response.isInUserSpace());
        ProfileDescription profileDesc = (ProfileDescription) response.getDescription();
        assertNotNull(profileDesc);
        assertEquals("Test1", profileDesc.getName());
        assertEquals("My Test", profileDesc.getDescription());
        assertEquals("J.Unit", profileDesc.getCreatorName());
        assertTrue(profileDesc.getId().startsWith(ComponentRegistry.REGISTRY_ID + "p_"));
        assertNotNull(profileDesc.getRegistrationDate());
        assertEquals("http://localhost:9998/registry/profiles/" + profileDesc.getId() + "?userspace=true", profileDesc.getHref());

        profiles = getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.APPLICATION_XML).get(PROFILE_LIST_GENERICTYPE);
        assertEquals(1, profiles.size());
        assertEquals(0, getAuthenticatedResource("/registry/profiles").accept(MediaType.APPLICATION_XML).get(PROFILE_LIST_GENERICTYPE)
                .size());
        ClientResponse cResponse = getResource().path("/registry/profiles/" + profileDesc.getId()).accept(MediaType.APPLICATION_XML).get(
                ClientResponse.class);
        assertEquals(204, cResponse.getStatus());
        CMDComponentSpec spec = getAuthenticatedResource(
                getResource().path("/registry/profiles/" + profileDesc.getId()).queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.APPLICATION_XML).get(CMDComponentSpec.class);
        assertNotNull(spec);

        cResponse = getResource().path("/registry/profiles/" + profileDesc.getId() + "/xsd").accept(MediaType.TEXT_XML).get(
                ClientResponse.class);
        assertEquals(204, cResponse.getStatus());
        String profile = getAuthenticatedResource(
                getResource().path("/registry/profiles/" + profileDesc.getId() + "/xsd").queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.length() > 0);

        profile = getAuthenticatedResource(
                getResource().path("/registry/profiles/" + profileDesc.getId() + "/xml").queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.length() > 0);

        cResponse = getAuthenticatedResource(
                getResource().path("/registry/profiles/" + profileDesc.getId()).queryParam(USERSPACE_PARAM, "true")).delete(
                ClientResponse.class);
        assertEquals(200, cResponse.getStatus());

        profiles = getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.APPLICATION_XML).get(PROFILE_LIST_GENERICTYPE);
        assertEquals(0, profiles.size());
    }

    private FormDataMultiPart createFormData(Object content) {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, content, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "Test1");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, "My Test");
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD, "My domain");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "TestGroup");
        return form;
    }

    @Test
    public void testRegisterWithUserComponents() throws Exception {
        ComponentRegistry userRegistry = ComponentRegistryFactory.getInstance().getComponentRegistry(true, DummyPrincipal.DUMMY_PRINCIPAL);
        String content = "";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"XXX\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
        content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ComponentDescription compDesc1 = RegistryTestHelper.addComponent(userRegistry, "XXX1", content);

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        FormDataMultiPart form = createFormData(content);
        RegisterResponse response = getAuthenticatedResource("/registry/components").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(response.isRegistered());
        assertEquals(1, response.getErrors().size());
        assertEquals("referenced component cannot be found in the published components: "+ compDesc1.getName()+" ("+ compDesc1.getId()+")", response.getErrors().get(0));

        response = getAuthenticatedResource(getResource().path("/registry/components").queryParam(USERSPACE_PARAM, "true")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        assertTrue(response.isRegistered());
        ComponentDescription comp2 = (ComponentDescription) response.getDescription();

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"ZZZ\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
        content += "        <CMD_Component ComponentId=\"" + comp2.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";

        form = createFormData(content);
        response = getAuthenticatedResource("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        assertFalse(response.isRegistered());
        assertEquals(1, response.getErrors().size());
        assertEquals("referenced component cannot be found in the published components: " + comp2.getName()+" ("+ comp2.getId()+")", response.getErrors().get(0));

        response = getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, "true")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        assertTrue(response.isRegistered());
    }

    @Test
    public void testRegisterUserspaceComponent() throws Exception {
        List<ComponentDescription> components = getAuthenticatedResource(
                getResource().path("/registry/components").queryParam(USERSPACE_PARAM, "true")).accept(MediaType.APPLICATION_XML).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals("user registered components", 0, components.size());
        assertEquals("public registered components", 0, getAuthenticatedResource("/registry/components").accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE).size());
        FormDataMultiPart form = createFormData(RegistryTestHelper.getComponentTestContent());

        RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/components").queryParam(USERSPACE_PARAM, "true"))
                .type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        assertTrue(response.isInUserSpace());
        ComponentDescription desc = (ComponentDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("Test1", desc.getName());
        assertEquals("My Test", desc.getDescription());
        assertEquals("J.Unit", desc.getCreatorName());
        assertEquals("TestGroup", desc.getGroupName());
        assertTrue(desc.getId().startsWith(ComponentRegistry.REGISTRY_ID + "c_"));
        assertNotNull(desc.getRegistrationDate());
        String url = getResource().getUriBuilder().build().toString();
        assertEquals(url + "registry/components/" + desc.getId() + "?userspace=true", desc.getHref());

        components = getAuthenticatedResource(getResource().path("/registry/components").queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.APPLICATION_XML).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(1, components.size());
        assertEquals(0, getAuthenticatedResource("/registry/components").accept(MediaType.APPLICATION_XML).get(COMPONENT_LIST_GENERICTYPE)
                .size());

        ClientResponse cResponse = getResource().path("/registry/components/" + desc.getId()).accept(MediaType.APPLICATION_XML).get(
                ClientResponse.class);
        assertEquals(204, cResponse.getStatus());
        CMDComponentSpec spec = getAuthenticatedResource(
                getResource().path("/registry/components/" + desc.getId()).queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.APPLICATION_XML).get(CMDComponentSpec.class);
        assertNotNull(spec);

        cResponse = getResource().path("/registry/components/" + desc.getId() + "/xsd").accept(MediaType.TEXT_XML)
                .get(ClientResponse.class);
        assertEquals(204, cResponse.getStatus());
        String result = getAuthenticatedResource(
                getResource().path("/registry/components/" + desc.getId() + "/xsd").queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.TEXT_XML).get(String.class);
        assertTrue(result.length() > 0);

        result = getAuthenticatedResource(
                getResource().path("/registry/components/" + desc.getId() + "/xml").queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.TEXT_XML).get(String.class);
        assertTrue(result.length() > 0);

        cResponse = getAuthenticatedResource(getResource().path("/registry/components/" + desc.getId()).queryParam(USERSPACE_PARAM, "true"))
                .delete(ClientResponse.class);
        assertEquals(200, cResponse.getStatus());

        components = getAuthenticatedResource(getResource().path("/registry/components").queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.APPLICATION_XML).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(0, components.size());
    }

    @Test
    public void testRegisterComponent() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, RegistryTestHelper.getComponentTestContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "ComponentTest1");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, "My Test Component");
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD, "TestDomain");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "TestGroup");
        RegisterResponse response = getAuthenticatedResource("/registry/components").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        assertFalse(response.isInUserSpace());
        ComponentDescription desc = (ComponentDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("ComponentTest1", desc.getName());
        assertEquals("My Test Component", desc.getDescription());
        assertEquals("J.Unit", desc.getCreatorName());
        assertEquals("TestGroup", desc.getGroupName());
        assertEquals("TestDomain", desc.getDomainName());
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
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD, "Domain");
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
        assertEquals(MDValidator.PARSE_ERROR, response.getErrors().get(1).substring(0, MDValidator.PARSE_ERROR.length()));
    }

    @Test
    public void testRegisterComponentAsProfile() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, RegistryTestHelper.getComponentTestContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "t");
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD, "domain");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, "My Test");
        RegisterResponse response = getAuthenticatedResource("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(response.isRegistered());
        assertTrue(response.isProfile());
        assertEquals(1, response.getErrors().size());
        assertEquals(MDValidator.MISMATCH_ERROR, response.getErrors().get(0));
    }
    
    @Test
    public void testPingSession() throws Exception {
         ClientResponse clientResponse = getAuthenticatedResource("/registry/pingSession").get(ClientResponse.class);
         assertEquals(200, clientResponse.getStatus());
         assertEquals("<session stillActive=\"true\"/>", clientResponse.getEntity(String.class));
    }

}
