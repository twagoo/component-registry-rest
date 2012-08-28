/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author olhsha
 */

public class CMDComponentTypeWrapperTest {

    ///////////////////////////////
    private CMDComponentType copyCMDComponentType(CMDComponentType newcomponent) {
       
       CMDComponentType component= new CMDComponentType();
       
       // filing in the important fields: fiename 
       component.setFilename(newcomponent.getFilename());
       
       // filing in the important fields: the list of child components
       List<CMDComponentType> childcomponents = component.getCMDComponent();
       boolean emptynesscheck=childcomponents.isEmpty();
       assertTrue(emptynesscheck);
       
       List<CMDComponentType> newchildcomponents = newcomponent.getCMDComponent();
       for (CMDComponentType currentcomponent: newchildcomponents){
           childcomponents.add(copyCMDComponentType(currentcomponent));
       }
       
       
       return component;
    }
    
    
    //////////////////////////
    private CMDComponentType makeTestComponent(){
       CMDComponentType component= new CMDComponentType();
       
       // fil-in filename
      component.setFilename("rhabarber");
      assertEquals(component.getFilename(), "rhabarber");
      
       // fil-in child components
      List<CMDComponentType> childcomponents = component.getCMDComponent();
      boolean emptynesscheck=childcomponents.isEmpty();
      assertTrue(emptynesscheck);
      
      String[] filenames = {"Guilherme", "Peter", "Twan", "Olha"};
      for (String currentname : filenames){
         CMDComponentType currentcomponent = new CMDComponentType();
         currentcomponent.setFilename(currentname);
         assertEquals(currentcomponent.getFilename(), currentname);
         childcomponents.add(currentcomponent);
       }
       
       
       return component;
    }
    
    
    
    
    ///////////////////////////////
    // checks if all the filenames related to the component are nulls
    private void checkNullnessOfFilenamesInComponent(CMDComponentType component){
        
       
      assertEquals(component.getFilename(), null);
      checkNullnessOfFilenamesInListOfComponents(component.getCMDComponent());  
     
     
    }
    
    ///////////////////////////////
    private void checkNullnessOfFilenamesInListOfComponents(List<CMDComponentType> listofcomponents){
        
      
      for (CMDComponentType currentcomponent : listofcomponents){
          checkNullnessOfFilenamesInComponent(currentcomponent);
      }
     
     
    }
    
    
    ///////////////////////////////
    // true if all the filenames for the inpit component are not null
    private boolean checkNonNullnessOfFilenamesInComponent(CMDComponentType component){
      
      String filename = component.getFilename();
      System.out.println(filename);
      boolean check=(filename == null);  
      
      if (check) return false;
      
      return checkNonNullnessOfFilenamesInListOfComponents(component.getCMDComponent());
    }
    
    ////////////////// /////////////
    // true if all thefilenames in the list of components are not null
    private boolean checkNonNullnessOfFilenamesInListOfComponents(List<CMDComponentType> listofcomponents){
        
        boolean check;
      
      for (CMDComponentType currentcomponent : listofcomponents){
          check = checkNonNullnessOfFilenamesInComponent(currentcomponent);
          if(!check) return false;
      }
     
     return true;
    }
    
    ///////////////////////////
    @Test    
    public void setFileNamesToNullTestComponent(){
       
      
      /////////////////////////////////////////////////////
      // check on a fresh (empty)  CMDComponentType
      CMDComponentType emptycomponent= new CMDComponentType();
      CMDComponentTypeWrapper emptywrapper = new CMDComponentTypeWrapper(emptycomponent);
      emptywrapper.setFileNamesToNull(); 
      assertEquals(emptycomponent.getFilename(), null);
      
      
      
      /////////////////////////////////////////////////////
      // check on  the test component  CMDComponentType
      
      CMDComponentType component= makeTestComponent();
      assertTrue(checkNonNullnessOfFilenamesInComponent(component));
      
      CMDComponentTypeWrapper wrapper = new CMDComponentTypeWrapper(component);
      wrapper.setFileNamesToNull();
      checkNullnessOfFilenamesInComponent(component);
      
      
      
      
      
    }
    
    
    @Test    
    public void setFileNamesToNullTestSpec(){
      
      // make test spec
      CMDComponentSpec componentspec= new CMDComponentSpec();  
        
      CMDComponentType component= makeTestComponent();
      CMDComponentType anothercomponent= copyCMDComponentType(component);
      
      
      List<CMDComponentType> listofcomponents = componentspec.getCMDComponent();
      listofcomponents.add(component);
      listofcomponents.add(anothercomponent);
      
      CMDComponentTypeWrapper wrapper = new CMDComponentTypeWrapper(componentspec);
      wrapper.setFileNamesToNull();
      
      checkNullnessOfFilenamesInListOfComponents(listofcomponents);
    }
      
      
    
    // adding dummy filenames to a component
    private void addDummyFilenamesToComponent(CMDComponentType component){
        
   
        if (component != null) {
        component.setFilename("Dummy");
        
        List<CMDComponentType> listofcomponents = component.getCMDComponent();
        
        addDummyFilenamesToListOfComponents(listofcomponents);
        
        }
    }
    
    // adding dummy filenames to list of  component
    private void addDummyFilenamesToListOfComponents(List<CMDComponentType> listofcomponents){
        
        
        for (CMDComponentType currentcomponent : listofcomponents) { 
                addDummyFilenamesToComponent(currentcomponent);
                } 
    
        
        
    }
    
      // adds dummy filenames to the content of largeProfile.XML  
      private  CMDComponentSpec makeTestSpecFromLargeProfile() throws IOException, JAXBException {
          
          String largeprofilestring = RegistryTestHelper.getLargeProfileContent(); // reading from the file
          CMDComponentSpec compspec=RegistryTestHelper.getComponentFromString(largeprofilestring); // calling unmarchaller
          
          List<CMDComponentType> listofcomponents = compspec.getCMDComponent();
          
          addDummyFilenamesToListOfComponents(listofcomponents);
          
          assertTrue(checkNonNullnessOfFilenamesInListOfComponents(listofcomponents));
          
          
          
          return compspec;
          
      }
      
       //writing profile file into the directory 
      private void writeSpecToFile(CMDComponentSpec compspec) throws IOException, JAXBException {
          
        String  os = RegistryTestHelper.getXml(compspec);
        
        
        FileOutputStream fop = null;
        File file;
        
        file = new File("src/test/resources/xml/Olhatest.xml");
        fop = new FileOutputStream(file);
        
        fop.write(os.getBytes());
	
        fop.flush();
	fop.close();
 
	System.out.println("Done");
 
        
      }
      
      @Test
      
      public void writeDummiedXML() throws IOException, JAXBException{
          
          CMDComponentSpec compspec=makeTestSpecFromLargeProfile();
          writeSpecToFile(compspec);
          
          
          
      }
        
      
      private String getProfileContentFromFile(String filename) throws IOException {
	InputStream dummiedProfileStream = RegistryTestHelper.class.getResourceAsStream(filename);
	try {
	    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dummiedProfileStream));
	    StringBuilder profileStringBuilder = new StringBuilder();
	    String line;
	    while (null != (line = bufferedReader.readLine())) {
		profileStringBuilder.append(line);
	    }
	    return profileStringBuilder.toString();
	} finally {
	    dummiedProfileStream.close();
	}
    }
      
            
      @Test
      
      public void setFileNamesToNullTestFile() throws IOException, JAXBException {
          
         CMDComponentSpec compspec  = makeTestSpecFromLargeProfile();
         writeSpecToFile(compspec);
         
         String dummiedcontent = getProfileContentFromFile("xml/Olhatest.xml");
         CMDComponentSpec newcompspec  = RegistryTestHelper.getComponentFromString(dummiedcontent); // calling unmarchaller
         
         
         CMDComponentTypeWrapper wrapper = new CMDComponentTypeWrapper(newcompspec);
         
         wrapper.setFileNamesToNull();
         
         checkNullnessOfFilenamesInListOfComponents(newcompspec.getCMDComponent());
      }
      
      
     
    }
    
    
    
    

