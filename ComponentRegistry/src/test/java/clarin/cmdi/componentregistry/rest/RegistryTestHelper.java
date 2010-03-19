package clarin.cmdi.componentregistry.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.bind.JAXBException;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

/**
 * Static helper methods to be used in tests
 * 
 */
public final class RegistryTestHelper {

    private RegistryTestHelper() {
    }

    public static ComponentDescription addComponent(ComponentRegistry testRegistry, String id) throws ParseException, JAXBException {
        return addComponent(testRegistry, id, getComponentTestContent());
    }

    public static ComponentDescription addComponent(ComponentRegistry testRegistry, String id, String content) throws ParseException,
            JAXBException {
        return addComponent(testRegistry, id, new ByteArrayInputStream(content.getBytes()));
    }

    private static ComponentDescription addComponent(ComponentRegistry testRegistry, String id, InputStream content) throws ParseException,
            JAXBException {
        ComponentDescription desc = ComponentDescription.createNewDescription();
        desc.setCreatorName(DummyPrincipal.DUMMY_PRINCIPAL.getName());
        desc.setName("testComponent");
        desc.setRegistrationDate("" + SimpleDateFormat.getDateInstance(DateFormat.SHORT).parse("1/1/2009"));
        desc.setDescription("Test Description");
        desc.setId(id);
        desc.setHref("link:" + id);
        CMDComponentSpec spec = MDMarshaller.unmarshal(CMDComponentSpec.class, content, MDMarshaller.getCMDComponentSchema());
        testRegistry.registerMDComponent(desc, spec);
        return desc;
    }

    public static InputStream getTestProfileContent() {
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        profileContent += "    <Header />\n";
        profileContent += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
        profileContent += "        <AttributeList>\n";
        profileContent += "            <Attribute>\n";
        profileContent += "                <Name>Name</Name>\n";
        profileContent += "                <Type>string</Type>\n";
        profileContent += "            </Attribute>\n";
        profileContent += "        </AttributeList>\n";
        profileContent += "        <CMD_Element name=\"Age\">\n";
        profileContent += "            <ValueScheme>\n";
        profileContent += "                <pattern>[23][0-9]</pattern>\n";
        profileContent += "            </ValueScheme>\n";
        profileContent += "        </CMD_Element>\n";
        profileContent += "    </CMD_Component>\n";
        profileContent += "</CMD_ComponentSpec>\n";
        return new ByteArrayInputStream(profileContent.getBytes());
    }

    public static ProfileDescription addProfile(ComponentRegistry testRegistry, String id) throws ParseException, JAXBException {
        return addProfile(testRegistry, id, RegistryTestHelper.getTestProfileContent());
    }

    public static ProfileDescription addProfile(ComponentRegistry testRegistry, String id, String content) throws ParseException,
            JAXBException {
        return addProfile(testRegistry, id, new ByteArrayInputStream(content.getBytes()));
    }

    private static ProfileDescription addProfile(ComponentRegistry testRegistry, String id, InputStream content) throws ParseException,
            JAXBException {
        ProfileDescription desc = ProfileDescription.createNewDescription();
        desc.setCreatorName(DummyPrincipal.DUMMY_PRINCIPAL.getName());
        desc.setName("testProfile");
        desc.setRegistrationDate("" + SimpleDateFormat.getDateInstance(DateFormat.SHORT).parse("1/1/2009"));
        desc.setDescription("Test Description");
        desc.setId(id);
        desc.setHref("link:" + id);
        CMDComponentSpec spec = MDMarshaller.unmarshal(CMDComponentSpec.class, content, MDMarshaller.getCMDComponentSchema());
        testRegistry.registerMDProfile(desc, spec);
        return desc;
    }

    public static CMDComponentSpec getTestProfile() throws JAXBException {
        return MDMarshaller.unmarshal(CMDComponentSpec.class, getTestProfileContent(), MDMarshaller.getCMDComponentSchema());
    }

    public static InputStream getComponentTestContent() {
        String compContent = "";
        compContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        compContent += "\n";
        compContent += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        compContent += "    xsi:noNamespaceSchemaLocation=\"../../general-component-schema.xsd\">\n";
        compContent += "    \n";
        compContent += "    <Header/>\n";
        compContent += "    \n";
        compContent += "    <CMD_Component name=\"Access\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
        compContent += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        compContent += "        <CMD_Element name=\"Date\">\n";
        compContent += "            <ValueScheme>\n";
        compContent += "                <!-- matching dates of the pattern yyyy-mm-dd (ISO 8601); this only matches dates from the years 1000 through 2999 and does allow some invalid dates (e.g. February, the 30th) -->\n";
        compContent += "                <pattern>(1|2)\\d{3}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])</pattern>                \n";
        compContent += "            </ValueScheme>\n";
        compContent += "        </CMD_Element>\n";
        compContent += "        <CMD_Element name=\"Owner\" ValueScheme=\"string\" />\n";
        compContent += "        <CMD_Element name=\"Publisher\" ValueScheme=\"string\" />\n";
        compContent += "    </CMD_Component>\n";
        compContent += "\n";
        compContent += "</CMD_ComponentSpec>\n";
        return new ByteArrayInputStream(compContent.getBytes());
    }

    public static CMDComponentSpec getTestComponent() throws JAXBException {
        return MDMarshaller.unmarshal(CMDComponentSpec.class, getComponentTestContent(), MDMarshaller.getCMDComponentSchema());
    }
}
