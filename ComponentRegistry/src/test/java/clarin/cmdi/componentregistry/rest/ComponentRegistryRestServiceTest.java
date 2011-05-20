package clarin.cmdi.componentregistry.rest;

import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.USERSPACE_PARAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.impl.filesystem.ComponentRegistryFactoryImpl;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.multipart.FormDataMultiPart;

public abstract class ComponentRegistryRestServiceTest extends ComponentRegistryRestServiceTestCase {


    protected abstract ComponentRegistry getTestRegistry();

    private void fillUp() throws Exception {
        RegistryTestHelper.addProfile(getTestRegistry(), "profile1");
        RegistryTestHelper.addProfile(getTestRegistry(), "profile2");
        RegistryTestHelper.addComponent(getTestRegistry(), "component1");
        RegistryTestHelper.addComponent(getTestRegistry(), "component2");
    };

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
        List<ComponentDescription> response = getUserComponents();
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
        ComponentDescription compDesc1 = RegistryTestHelper.addComponent(getTestRegistry(), "XXX1", content);

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ComponentDescription compDesc2 = RegistryTestHelper.addComponent(getTestRegistry(), "YYY1", content);

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"ZZZ\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ProfileDescription profile = RegistryTestHelper.addProfile(getTestRegistry(), "TestProfile3", content);

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
            fail("Exception should have been thrown resource does not exist, HttpStatusCode 204");
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
    public void testPrivateProfileXsd() throws Exception {
        FormDataMultiPart form = createFormData(RegistryTestHelper.getTestProfileContent());
        RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, "true"))
                .type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        assertTrue(response.isProfile());
        assertEquals(1, getUserProfiles().size());
        AbstractDescription desc = response.getDescription();
        String profile = getResource().path("/registry/profiles/" + desc.getId() + "/xsd").accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema"));
        assertTrue(profile.endsWith("</xs:schema>"));
    }

    @Test
    public void testPrivateComponentXsd() throws Exception {
        FormDataMultiPart form = createFormData(RegistryTestHelper.getComponentTestContent());
        RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/components").queryParam(USERSPACE_PARAM, "true"))
                .type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        assertFalse(response.isProfile());
        assertEquals(1, getUserComponents().size());
        AbstractDescription desc = response.getDescription();
        String profile = getResource().path("/registry/components/" + desc.getId() + "/xsd").accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema"));
        assertTrue(profile.endsWith("</xs:schema>"));
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
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "My Test Group");
        RegisterResponse response = getAuthenticatedResource("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertTrue(response.isProfile());
        assertFalse(response.isInUserSpace());
        ProfileDescription profileDesc = (ProfileDescription) response.getDescription();
        assertNotNull(profileDesc);
        assertEquals("ProfileTest1", profileDesc.getName());
        assertEquals("My Test Profile", profileDesc.getDescription());
        assertEquals("TestDomain", profileDesc.getDomainName());
        assertEquals("My Test Group", profileDesc.getGroupName());
        assertEquals(expectedUserId("JUnit@test.com"), profileDesc.getUserId());
        assertEquals("JUnit@test.com", profileDesc.getCreatorName());
        assertTrue(profileDesc.getId().startsWith(ComponentRegistry.REGISTRY_ID + "p_"));
        assertNotNull(profileDesc.getRegistrationDate());
        assertEquals("http://localhost:9998/registry/profiles/" + profileDesc.getId(), profileDesc.getHref());
    }

    protected abstract String expectedUserId(String principal);

    @Test
    public void testPublishProfile() throws Exception {
        assertEquals("user registered profiles", 0, getUserProfiles().size());
        assertEquals("public registered profiles", 0, getPublicProfiles().size());
        FormDataMultiPart form = createFormData(RegistryTestHelper.getTestProfileContent(), "Unpublished");
        RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, "true"))
                .type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        assertTrue(response.isProfile());
        AbstractDescription desc = response.getDescription();
        assertEquals("Unpublished", desc.getDescription());
        assertEquals(1, getUserProfiles().size());
        assertEquals(0, getPublicProfiles().size());
        form = createFormData(RegistryTestHelper.getTestProfileContent("publishedName"), "Published");
        response = getAuthenticatedResource(getResource().path("/registry/profiles/" + desc.getId() + "/publish")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);

        assertEquals(0, getUserProfiles().size());
        List<ProfileDescription> profiles = getPublicProfiles();
        assertEquals(1, profiles.size());
        ProfileDescription profileDescription = profiles.get(0);
        assertNotNull(profileDescription.getId());
        assertEquals(desc.getId(), profileDescription.getId());
        assertEquals("http://localhost:9998/registry/profiles/" + desc.getId(), profileDescription.getHref());
        assertEquals("Published", profileDescription.getDescription());
        CMDComponentSpec spec = getPublicSpec(profileDescription);
        assertEquals("publishedName", spec.getCMDComponent().get(0).getName());
    }

    private CMDComponentSpec getPublicSpec(AbstractDescription desc) {
        if (desc.isProfile()) {
            return getResource().path("/registry/profiles/" + desc.getId()).get(CMDComponentSpec.class);
        } else {
            return getResource().path("/registry/components/" + desc.getId()).get(CMDComponentSpec.class);
        }
    }

    @Test
    public void testPublishComponent() throws Exception {
        assertEquals(0, getUserComponents().size());
        assertEquals(0, getPublicComponents().size());
        FormDataMultiPart form = createFormData(RegistryTestHelper.getComponentTestContent(), "Unpublished");
        RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/components").queryParam(USERSPACE_PARAM, "true"))
                .type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        assertFalse(response.isProfile());
        AbstractDescription desc = response.getDescription();
        assertEquals("Unpublished", desc.getDescription());
        assertEquals(1, getUserComponents().size());
        assertEquals(0, getPublicComponents().size());
        form = createFormData(RegistryTestHelper.getComponentTestContent("publishedName"), "Published");
        response = getAuthenticatedResource(getResource().path("/registry/components/" + desc.getId() + "/publish")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);

        assertEquals(0, getUserComponents().size());
        List<ComponentDescription> components = getPublicComponents();
        assertEquals(1, components.size());
        ComponentDescription componentDescription = components.get(0);
        assertNotNull(componentDescription.getId());
        assertEquals(desc.getId(), componentDescription.getId());
        assertEquals("http://localhost:9998/registry/components/" + desc.getId(), componentDescription.getHref());
        assertEquals("Published", componentDescription.getDescription());
        CMDComponentSpec spec = getPublicSpec(componentDescription);
        assertEquals("publishedName", spec.getCMDComponent().get(0).getName());
    }

    @Test
    public void testRegisterUserspaceProfile() throws Exception {
        List<ProfileDescription> profiles = getUserProfiles();
        assertEquals("user registered profiles", 0, profiles.size());
        assertEquals("public registered profiles", 0, getPublicProfiles().size());
        FormDataMultiPart form = createFormData(RegistryTestHelper.getTestProfileContent());
        RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, "true"))
                .type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        assertTrue(response.isProfile());
        assertTrue(response.isInUserSpace());
        ProfileDescription profileDesc = (ProfileDescription) response.getDescription();
        assertNotNull(profileDesc);
        assertEquals("Test1", profileDesc.getName());
        assertEquals("My Test", profileDesc.getDescription());
        assertEquals(expectedUserId("JUnit@test.com"), profileDesc.getUserId());
        assertEquals("JUnit@test.com", profileDesc.getCreatorName());
        assertTrue(profileDesc.getId().startsWith(ComponentRegistry.REGISTRY_ID + "p_"));
        assertNotNull(profileDesc.getRegistrationDate());
        assertEquals("http://localhost:9998/registry/profiles/" + profileDesc.getId() + "?userspace=true", profileDesc.getHref());

        profiles = getUserProfiles();
        assertEquals(1, profiles.size());
        assertEquals(0, getPublicProfiles().size());
        ClientResponse cResponse = getResource().path("/registry/profiles/" + profileDesc.getId()).accept(MediaType.APPLICATION_XML).get(
                ClientResponse.class);
        assertEquals(204, cResponse.getStatus());
        CMDComponentSpec spec = getAuthenticatedResource(
                getResource().path("/registry/profiles/" + profileDesc.getId()).queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.APPLICATION_XML).get(CMDComponentSpec.class);
        assertNotNull(spec);

        cResponse = getResource().path("/registry/profiles/" + profileDesc.getId() + "/xsd").accept(MediaType.TEXT_XML).get(
                ClientResponse.class);
        assertEquals(200, cResponse.getStatus());
        String profile = cResponse.getEntity(String.class);
        assertTrue(profile.length() > 0);

        profile = getAuthenticatedResource(getResource().path("/registry/profiles/" + profileDesc.getId() + "/xml")).accept(
                MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.length() > 0);

        cResponse = getAuthenticatedResource(
                getResource().path("/registry/profiles/" + profileDesc.getId()).queryParam(USERSPACE_PARAM, "true")).delete(
                ClientResponse.class);
        assertEquals(200, cResponse.getStatus());

        profiles = getUserProfiles();
        assertEquals(0, profiles.size());
    }

    private List<ProfileDescription> getPublicProfiles() {
        return getAuthenticatedResource("/registry/profiles").accept(MediaType.APPLICATION_XML).get(PROFILE_LIST_GENERICTYPE);
    }

    private List<ProfileDescription> getUserProfiles() {
        return getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.APPLICATION_XML).get(PROFILE_LIST_GENERICTYPE);
    }

    private FormDataMultiPart createFormData(Object content) {
        return createFormData(content, "My Test");
    }

    private FormDataMultiPart createFormData(Object content, String description) {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, content, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "Test1");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, description);
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD, "My domain");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "TestGroup");
        return form;
    }

    protected abstract ComponentRegistryFactory getRegistryFactory();

    @Test
    public void testRegisterWithUserComponents() throws Exception {
        ComponentRegistry userRegistry = getRegistryFactory().getComponentRegistry(true, DummyPrincipal.DUMMY_CREDENTIALS);
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
        assertEquals("referenced component cannot be found in the published components: " + compDesc1.getName() + " (" + compDesc1.getId()
                + ")", response.getErrors().get(0));

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
        assertEquals("referenced component cannot be found in the published components: " + comp2.getName() + " (" + comp2.getId() + ")",
                response.getErrors().get(0));

        response = getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, "true")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        assertTrue(response.isRegistered());
    }

    @Test
    public void testRegisterUserspaceComponent() throws Exception {
        List<ComponentDescription> components = getUserComponents();
        assertEquals("user registered components", 0, components.size());
        assertEquals("public registered components", 0, getPublicComponents().size());
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
        assertEquals(expectedUserId("JUnit@test.com"), desc.getUserId());
        assertEquals("JUnit@test.com", desc.getCreatorName());
        assertEquals("TestGroup", desc.getGroupName());
        assertTrue(desc.getId().startsWith(ComponentRegistry.REGISTRY_ID + "c_"));
        assertNotNull(desc.getRegistrationDate());
        String url = getResource().getUriBuilder().build().toString();
        assertEquals(url + "registry/components/" + desc.getId() + "?userspace=true", desc.getHref());

        components = getUserComponents();
        assertEquals(1, components.size());
        assertEquals(0, getPublicComponents().size());

        ClientResponse cResponse = getResource().path("/registry/components/" + desc.getId()).accept(MediaType.APPLICATION_XML).get(
                ClientResponse.class);
        assertEquals(204, cResponse.getStatus());
        CMDComponentSpec spec = getUserComponent(desc);
        assertNotNull(spec);

        cResponse = getResource().path("/registry/components/" + desc.getId() + "/xsd").accept(MediaType.TEXT_XML)
                .get(ClientResponse.class);
        assertEquals(200, cResponse.getStatus());
        String result = cResponse.getEntity(String.class);
        assertTrue(result.length() > 0);

        result = getAuthenticatedResource(getResource().path("/registry/components/" + desc.getId() + "/xml")).accept(MediaType.TEXT_XML)
                .get(String.class);
        assertTrue(result.length() > 0);

        cResponse = getAuthenticatedResource(getResource().path("/registry/components/" + desc.getId()).queryParam(USERSPACE_PARAM, "true"))
                .delete(ClientResponse.class);
        assertEquals(200, cResponse.getStatus());

        components = getUserComponents();
        assertEquals(0, components.size());
    }

    @Test
    public void testUpdateComponent() throws Exception {
        List<ComponentDescription> components = getUserComponents();
        assertEquals("user registered components", 0, components.size());
        assertEquals("public registered components", 0, getPublicComponents().size());

        FormDataMultiPart form = createFormData(RegistryTestHelper.getComponentTestContent());
        ClientResponse cResponse = getAuthenticatedResource(getResource().path("/registry/components").queryParam(USERSPACE_PARAM, "true"))
                .type(MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
        RegisterResponse response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        assertTrue(response.isInUserSpace());
        ComponentDescription desc = (ComponentDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("Test1", desc.getName());
        assertEquals("My Test", desc.getDescription());
        Date firstDate = AbstractDescription.getDate(desc.getRegistrationDate());
        CMDComponentSpec spec = getUserComponent(desc);
        assertNotNull(spec);
        assertEquals("Access", spec.getCMDComponent().get(0).getName());
        components = getUserComponents();
        assertEquals(1, components.size());
        assertEquals(0, getPublicComponents().size());

        //Now update
        form = createFormData(RegistryTestHelper.getComponentTestContent("TESTNAME"), "UPDATE DESCRIPTION!");
        cResponse = getAuthenticatedResource(
                getResource().path("/registry/components/" + desc.getId() + "/update").queryParam(USERSPACE_PARAM, "true")).type(
                MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
        response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        assertTrue(response.isInUserSpace());
        desc = (ComponentDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("Test1", desc.getName());
        assertEquals("UPDATE DESCRIPTION!", desc.getDescription());
        Date secondDate = AbstractDescription.getDate(desc.getRegistrationDate());
        assertTrue(firstDate.before(secondDate) || firstDate.equals(secondDate));

        spec = getUserComponent(desc);
        assertNotNull(spec);
        assertEquals("TESTNAME", spec.getCMDComponent().get(0).getName());
        components = getUserComponents();
        assertEquals(1, components.size());
        assertEquals(0, getPublicComponents().size());
    }

    @Test
    public void testUpdateProfile() throws Exception {
        List<ProfileDescription> profiles = getUserProfiles();
        assertEquals(0, profiles.size());
        assertEquals(0, getPublicProfiles().size());

        FormDataMultiPart form = createFormData(RegistryTestHelper.getTestProfileContent());
        ClientResponse cResponse = getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(USERSPACE_PARAM, "true"))
                .type(MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
        RegisterResponse response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertTrue(response.isProfile());
        assertTrue(response.isInUserSpace());
        ProfileDescription desc = (ProfileDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("Test1", desc.getName());
        assertEquals("My Test", desc.getDescription());
        assertEquals("TestGroup", desc.getGroupName());
        Date firstDate = AbstractDescription.getDate(desc.getRegistrationDate());
        CMDComponentSpec spec = getUserProfile(desc);
        assertNotNull(spec);
        assertEquals("Actor", spec.getCMDComponent().get(0).getName());
        profiles = getUserProfiles();
        assertEquals(1, profiles.size());
        assertEquals(0, getPublicComponents().size());

        //Now update
        form = createFormData(RegistryTestHelper.getTestProfileContent("TESTNAME"), "UPDATE DESCRIPTION!");
        cResponse = getAuthenticatedResource(
                getResource().path("/registry/profiles/" + desc.getId() + "/update").queryParam(USERSPACE_PARAM, "true")).type(
                MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
        response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertTrue(response.isProfile());
        assertTrue(response.isInUserSpace());
        desc = (ProfileDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("Test1", desc.getName());
        assertEquals("UPDATE DESCRIPTION!", desc.getDescription());
        Date secondDate = AbstractDescription.getDate(desc.getRegistrationDate());
        assertTrue(firstDate.before(secondDate) || firstDate.equals(secondDate));

        spec = getUserProfile(desc);
        assertNotNull(spec);
        assertEquals("TESTNAME", spec.getCMDComponent().get(0).getName());
        profiles = getUserProfiles();
        assertEquals(1, profiles.size());
        assertEquals(0, getPublicComponents().size());
    }

    private CMDComponentSpec getUserComponent(ComponentDescription desc) {
        return getAuthenticatedResource(getResource().path("/registry/components/" + desc.getId()).queryParam(USERSPACE_PARAM, "true"))
                .accept(MediaType.APPLICATION_XML).get(CMDComponentSpec.class);
    }

    private CMDComponentSpec getUserProfile(ProfileDescription desc) {
        return getAuthenticatedResource(getResource().path("/registry/profiles/" + desc.getId()).queryParam(USERSPACE_PARAM, "true"))
                .accept(MediaType.APPLICATION_XML).get(CMDComponentSpec.class);
    }

    private List<ComponentDescription> getPublicComponents() {
        return getAuthenticatedResource("/registry/components").accept(MediaType.APPLICATION_XML).get(COMPONENT_LIST_GENERICTYPE);
    }

    private List<ComponentDescription> getUserComponents() {
        return getAuthenticatedResource(getResource().path("/registry/components").queryParam(USERSPACE_PARAM, "true")).accept(
                MediaType.APPLICATION_XML).get(COMPONENT_LIST_GENERICTYPE);
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
        assertEquals(expectedUserId("JUnit@test.com"), desc.getUserId());
        assertEquals("JUnit@test.com", desc.getCreatorName());
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
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "Group");
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
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "My Group");
        RegisterResponse response = getAuthenticatedResource("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(response.isRegistered());
        assertTrue(response.isProfile());
        assertEquals(1, response.getErrors().size());
        assertEquals(MDValidator.MISMATCH_ERROR, response.getErrors().get(0));
    }

    //    @Test
    //    public void testPingSession() throws Exception {
    //        ClientResponse clientResponse = getAuthenticatedResource("/registry/pingSession").get(ClientResponse.class);
    //        assertEquals(200, clientResponse.getStatus());
    //        assertEquals("<session stillActive=\"true\"/>", clientResponse.getEntity(String.class));
    //        clientResponse = getResource().path("/registry/pingSession").get(ClientResponse.class);
    //        assertEquals(200, clientResponse.getStatus());
    //        assertEquals("<session stillActive=\"false\"/>", clientResponse.getEntity(String.class));
    // TODO Patrick enable test and client, need to update dependencies first. Add some test with empty session.
    //    }
    //    @Override
    //    public Client client() {
    //      DefaultApacheHttpClientConfig config = new  
    //      DefaultApacheHttpClientConfig();
    //      config
    //      .setProperty("com.sun.jersey.impl.client.httpclient.handleCookies",  
    //      true);
    //      ApacheHttpClient c = ApacheHttpClient.create(config); 
    //      
    //        return ;
    //    }
}
