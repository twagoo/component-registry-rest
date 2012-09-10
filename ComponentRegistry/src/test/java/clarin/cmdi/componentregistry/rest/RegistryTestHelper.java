package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistryException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

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
	    JAXBException, UnsupportedEncodingException {
	return addComponent(testRegistry, id, new ByteArrayInputStream(content.getBytes("UTF-8")));
    }

    private static ComponentDescription addComponent(ComponentRegistry testRegistry, String id, InputStream content) throws ParseException,
	    JAXBException {
	ComponentDescription desc = ComponentDescription.createNewDescription();
	desc.setCreatorName(DummyPrincipal.DUMMY_CREDENTIALS.getDisplayName());
	desc.setUserId(DummyPrincipal.DUMMY_CREDENTIALS.getPrincipalName());
	desc.setName(id);
	desc.setDescription("Test Description");
	desc.setId(ComponentRegistry.REGISTRY_ID + id);
	desc.setHref("link:" + ComponentRegistry.REGISTRY_ID + id);
	CMDComponentSpec spec = MDMarshaller.unmarshal(CMDComponentSpec.class, content, MDMarshaller.getCMDComponentSchema());
	testRegistry.register(desc, spec);
	return desc;
    }

    public static int updateComponent(ComponentRegistry testRegistry, ComponentDescription description, String content) throws JAXBException {
	return testRegistry.update(description, getComponentFromString(content), DummyPrincipal.DUMMY_CREDENTIALS.getPrincipal(), true);
    }

    public static String getProfileTestContentString() {
	return getProfileTestContentString("Actor");
    }

    private static String getProfileTestContentString(String name) {
	String profileContent = "";
	profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	profileContent += "    <Header />\n";
	profileContent += "    <CMD_Component name=\"" + name + "\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
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
	return profileContent;
    }

    public static InputStream getTestProfileContent() {
	return getTestProfileContent("Actor");
    }

    public static InputStream getTestProfileContent(String name) {
	return new ByteArrayInputStream(getProfileTestContentString(name).getBytes());
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
	desc.setCreatorName(DummyPrincipal.DUMMY_CREDENTIALS.getDisplayName());
	desc.setUserId(DummyPrincipal.DUMMY_CREDENTIALS.getPrincipalName());
	desc.setName(id);
	desc.setDescription("Test Description");
	desc.setId(ComponentRegistry.REGISTRY_ID + id);
	desc.setHref("link:" + ComponentRegistry.REGISTRY_ID + id);
	CMDComponentSpec spec = MDMarshaller.unmarshal(CMDComponentSpec.class, content, MDMarshaller.getCMDComponentSchema());
	testRegistry.register(desc, spec);
	return desc;
    }

    public static CMDComponentSpec getTestProfile() throws JAXBException {
	return MDMarshaller.unmarshal(CMDComponentSpec.class, getTestProfileContent(), MDMarshaller.getCMDComponentSchema());
    }

    public static String getComponentTestContentString() {
	return getComponentTestContentString("Access");
    }

    public static InputStream getComponentTestContent() {
	return getComponentTestContent("Access");
    }

    public static String getComponentTestContentString(String componentName) {
	String compContent = "";
	compContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	compContent += "\n";
	compContent += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	compContent += "    xsi:noNamespaceSchemaLocation=\"../../general-component-schema.xsd\">\n";
	compContent += "    \n";
	compContent += "    <Header/>\n";
	compContent += "    \n";
	compContent += "    <CMD_Component name=\"" + componentName + "\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
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
	return compContent;
    }

    
    public static String getStringFromStream(InputStream largeProfileStream) throws IOException {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(largeProfileStream));
            StringBuilder profileStringBuilder = new StringBuilder();
            String line;
            while (null != (line = bufferedReader.readLine())) {
                profileStringBuilder.append(line);
            }
            return profileStringBuilder.toString();
        } finally {
            largeProfileStream.close();
        }
    }
    ///////////////////////////////////////////////////////
    
    public static String getLargeProfileContent() throws IOException {
	InputStream largeProfileStream = RegistryTestHelper.class.getResourceAsStream("/xml/largeProfile.xml");
        return getStringFromStream(largeProfileStream);
    }
    
    
    

    //////////////////////
    
    
    public static CMDComponentSpec getComponentFromString(String contentString) throws JAXBException {
	return MDMarshaller.unmarshal(CMDComponentSpec.class, getComponentContent(contentString), MDMarshaller.getCMDComponentSchema());
    }

    public static InputStream getComponentContent(String content) {
	return new ByteArrayInputStream(content.getBytes());
    }

    public static InputStream getComponentTestContent(String componentName) {
	return getComponentContent(getComponentTestContentString(componentName));
    }

    public static CMDComponentSpec getTestComponent() throws JAXBException {
	return MDMarshaller.unmarshal(CMDComponentSpec.class, getComponentTestContent(), MDMarshaller.getCMDComponentSchema());
    }

    public static CMDComponentSpec getTestComponent(String name) throws JAXBException {
	return MDMarshaller.unmarshal(CMDComponentSpec.class, getComponentTestContent(name), MDMarshaller.getCMDComponentSchema());
    }

    public static String getXml(CMDComponentSpec componentSpec) throws JAXBException, UnsupportedEncodingException {
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	MDMarshaller.marshal(componentSpec, os);
	String xml = os.toString();
	try {
	    os.close();
	} catch (IOException ex) {
	}
	return xml;
    }

    public static Comment addComment(ComponentRegistry testRegistry, String id, String descriptionId, String principal) throws ParseException, JAXBException, ComponentRegistryException {
	return addComment(testRegistry, RegistryTestHelper.getTestCommentContent(id, descriptionId), principal);
    }

    private static Comment addComment(ComponentRegistry testRegistry, InputStream content, String principal) throws ParseException,
	    JAXBException,
	    ComponentRegistryException {
	Comment spec = MDMarshaller.unmarshal(Comment.class, content, null);
	testRegistry.registerComment(spec, principal);
	return spec;
    }

    public static String getCommentTestContentStringForProfile(String commentName, String profileId) {
	String comContent = "";
	comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	comContent += "    <comments>" + commentName + "</comments>\n";
	comContent += "    <commentDate>" + Comment.createNewDate() + "</commentDate>\n";
	comContent += "    <profileDescriptionId>" + profileId + "</profileDescriptionId>\n";
	comContent += "    <userName>J. Unit</userName>\n";
	comContent += "</comment>\n";
	return comContent;
    }

    public static String getCommentTestContentStringForComponent(String commentName, String componentId) {
	String comContent = "";
	comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	comContent += "    <comments>" + commentName + "</comments>\n";
	comContent += "    <commentDate>" + Comment.createNewDate() + "</commentDate>\n";
	comContent += "     <componentDescriptionId>" + componentId + "</componentDescriptionId>";
	comContent += "    <userName>J. Unit</userName>\n";
	comContent += "</comment>\n";
	return comContent;
    }

    public static InputStream getTestCommentContent(String content, String descriptionId) {
	if (descriptionId.contains("profile")) {
	    return new ByteArrayInputStream(getCommentTestContentStringForProfile(content, descriptionId).getBytes());
	} else {
	    return new ByteArrayInputStream(getCommentTestContentStringForComponent(content, descriptionId).getBytes());
	}
    }

    public static String getCommentTestContent(String commentId, String descriptionId) {
	return getCommentTestContentStringForProfile(commentId, descriptionId);
    }

    public static InputStream getCommentTestContent() {
	return getTestCommentContent("Actual", "clarin.eu:cr1:profile1");
    }

    /**
     * Testing a big xsd string is a bit hard, so doing a best effort by checking the xs:element which represent the nested components used
     * in a profile/component
     */
    public static boolean hasComponent(String xsd, String name, String min, String max) {
	Pattern pattern = Pattern.compile("<xs:element name=\"" + name + "\" minOccurs=\"" + min + "\" maxOccurs=\"" + max + "\">");
	Matcher matcher = pattern.matcher(xsd);
	return matcher.find() && !matcher.find(); //find only one
    }
    
    
     // helping method writing byte[] bytes into the filename  
      public static void writeBytesToFile(byte[] bytes, String filename) throws IOException, JAXBException {
                 
        File file = new File(filename);
        FileOutputStream fop = new FileOutputStream(file);
        
        fop.write(bytes);
	
        fop.flush();
	fop.close();
 
        
      }
    
    
    
      //helping method writing String str  into the file filename
      public static void writeStringToFile(String str, String filename) throws IOException, JAXBException {
          
        writeBytesToFile(str.getBytes(), filename);
 
        
      }
      
      /////////////////////////////////////////
    // writing ByteArrayOutputStream into the file filename
      public static void writeStreamToFile(ByteArrayOutputStream os, String filename) throws IOException, JAXBException {
          
        writeBytesToFile(os.toByteArray(), filename);
 
        
      }
      
      //writing a component into the filename
    public static void writeComponentIntoFile(ComponentDescription cdesc, String filename) throws IOException, JAXBException {
        
       
        ByteArrayOutputStream os = new ByteArrayOutputStream();
	MDMarshaller.marshal(cdesc, os);
        
        writeStreamToFile(os, filename);
       
    }
      
      
      
      
      //creating/opening a local directory for your testing files, returns its absolute path
      public static String openTestDir(String dirName){
      
         File testDir = new File("target/" + dirName);
         
         
         testDir.mkdir();
         
         System.out.println(dirName);
         //String retval = new File(testDir, dirName).getAbsolutePath();
         String retval = new File(testDir, "/").getAbsolutePath();
        
         return(retval);
         
      }
      
}
