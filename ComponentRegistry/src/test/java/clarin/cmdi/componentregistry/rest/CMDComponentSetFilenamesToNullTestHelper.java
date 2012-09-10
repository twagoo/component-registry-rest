/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clarin.cmdi.componentregistry.rest;


import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author olhsha
 */
public class CMDComponentSetFilenamesToNullTestHelper {
    
   
/**
 *
 * @author olhsha
 */
    final ComponentRegistryRestService testrestservice = new ComponentRegistryRestService();
    
   
    public ComponentRegistryRestService getTestRestService(){
     return testrestservice;
    }

    //////////////////////////////////////////////////
    private CMDComponentType copyCMDComponentType(CMDComponentType newcomponent) {
       
       CMDComponentType component= new CMDComponentType();
       
       // filing in filename 
       component.setFilename(newcomponent.getFilename());
       
       // filing in the list of child components
       List<CMDComponentType> childcomponents = component.getCMDComponent();
       boolean emptynesscheck=childcomponents.isEmpty();
       assertTrue(emptynesscheck);
       
       List<CMDComponentType> newchildcomponents = newcomponent.getCMDComponent();
       for (CMDComponentType currentcomponent: newchildcomponents){
           childcomponents.add(copyCMDComponentType(currentcomponent));
       }
       
       
       return component;
    }
    
    
    ////////////////////////////////////////////
    
    
    public CMDComponentType makeTestComponent(){
       CMDComponentType component= new CMDComponentType();
       
       // fil-in filename
      component.setFilename("mini0.619");
      assertEquals(component.getFilename(), "mini0.619");
      
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
    
    
    
    
    /////////////////////////////////////////////
    // checks if all the filenames related to the component are nulls
    private void checkNullnessOfFilenamesInComponent(CMDComponentType component){
        
       
      assertEquals(component.getFilename(), null);
      checkNullnessOfFilenamesInListOfComponents(component.getCMDComponent());  
     
     
    }
    
    ///////////////////////////////
    // checks if all the filenames related to the list of component are nulls
    public void checkNullnessOfFilenamesInListOfComponents(List<CMDComponentType> listofcomponents){
        
      
      for (CMDComponentType currentcomponent : listofcomponents){
          checkNullnessOfFilenamesInComponent(currentcomponent);
      }
     
     
    }
    
    
    /////////////////////////////////////////////////
    // returns true if all the filenames related to the input component are not null
    private boolean checkNonNullnessOfFilenamesInComponent(CMDComponentType component){
      
      String filename = component.getFilename();
      System.out.println(filename);
      boolean check=(filename == null);  
      
      if (check) return false;
      
      return checkNonNullnessOfFilenamesInListOfComponents(component.getCMDComponent());
    }
    
    ////////////////// /////////////
    // returns true if all thefilenames related to the list of components are not null
    public boolean checkNonNullnessOfFilenamesInListOfComponents(List<CMDComponentType> listofcomponents){
        
        boolean check;
      
      for (CMDComponentType currentcomponent : listofcomponents){
          check = checkNonNullnessOfFilenamesInComponent(currentcomponent);
          if(!check) return false;
      }
     
     return true;
    }
    
    ////////////////////////////////////////
    
    
    @Test    
    public void setFileNamesToNullTestComponent(){
       
      
      
      // check  a fresh (empty)  CMDComponentType
      CMDComponentType emptycomponent= new CMDComponentType();
      testrestservice.setFileNamesToNullCurrent(emptycomponent);
      assertEquals(emptycomponent.getFilename(), null);
      
      
      
      // check  the test component  CMDComponentType
      
      CMDComponentType component= makeTestComponent();
      assertTrue(checkNonNullnessOfFilenamesInComponent(component));
      
      
      testrestservice.setFileNamesToNullCurrent(component);
      checkNullnessOfFilenamesInComponent(component);
      
    }
    
    
   
    
    //////////////////////////////////////////////////////////////
    /// Testing on the profile XML file 
    // There are a few auxiliary methods preceeding the test method, which is at the end
    
    // adding dummy filenames to a component
    private void addDummyFilenamesToComponent(CMDComponentType component){
        
   
        if (component != null) {
        component.setFilename("Dummy");
        
        List<CMDComponentType> listofcomponents = component.getCMDComponent();
        addDummyFilenamesToListOfComponents(listofcomponents);
        
        }
    }
    
    // adding dummy filenames to the list of  components
    public void addDummyFilenamesToListOfComponents(List<CMDComponentType> listofcomponents){
        
        
        for (CMDComponentType currentcomponent : listofcomponents) { 
                addDummyFilenamesToComponent(currentcomponent);
                } 
    
        
        
    }
    
      // adds dummy filenames to the content of largeProfile.XML  
      private  CMDComponentSpec makeTestFromFile(String filename) throws IOException, JAXBException {
          
          
          FileInputStream is = new FileInputStream(filename);
          
          
          
          String profilestring = RegistryTestHelper.getStringFromStream(is);
          CMDComponentSpec compspec=RegistryTestHelper.getComponentFromString(profilestring); // calling unmarchaller
          
          List<CMDComponentType> listofcomponents = compspec.getCMDComponent();
          addDummyFilenamesToListOfComponents(listofcomponents);
          
          assertTrue(checkNonNullnessOfFilenamesInListOfComponents(listofcomponents));
          
          return compspec;
          
      }
      
     
      
      
      // creating the profile with filenames filled by "Dummy" and writing it into the file 
      public void writeDummiedXML(String filenamein, String filenameout) throws IOException, JAXBException{
          
          
          CMDComponentSpec compspec=makeTestFromFile(filenamein);
          
          
          String  os = RegistryTestHelper.getXml(compspec);
          
          
          RegistryTestHelper.writeStringToFile(os, filenameout);
      }
      
      
      
      // generic test-from-file read/write
      public void setFileNamesToNullInFile(String dirName, String fileNameInit, String fileNameDummied, String fileNameUnDummied) throws IOException, JAXBException {
          
         String path = RegistryTestHelper.openTestDir(dirName);
   
         // make a file with a lot of "Dummy" filenames
         writeDummiedXML(path+fileNameInit, path+fileNameDummied);
         
         FileInputStream is = new FileInputStream(path+fileNameDummied);
         String dummiedcontent = RegistryTestHelper.getStringFromStream(is);
         CMDComponentSpec compspec  = RegistryTestHelper.getComponentFromString(dummiedcontent); // calling unmarchaller
         
         List<CMDComponentType> listofcomponents = compspec.getCMDComponent();
         testrestservice.setFileNamesFromListToNull(listofcomponents);
         checkNullnessOfFilenamesInListOfComponents(listofcomponents);
         
         String  os = RegistryTestHelper.getXml(compspec);
         RegistryTestHelper.writeStringToFile(os, path+fileNameUnDummied);
         
         
      }
      
      
      
     /////// developer test method: nulling  filenames in an arbitrary (component) file 
      
      public static void main(String args[])
        throws java.io.IOException , JAXBException {
            
            
          BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

          
            
            
            System.out.println("");  
            System.out.print("Sub-directory (of target/) name? (up to 32 symbols): ");
            String dirName=buffer.readLine();
            System.out.println("");  
            System.out.println("(Watch out: this is a temorary directory, which is removed after any new build)");
            System.out.println(dirName);
            
            System.out.println(""); 
            System.out.println("Check if your file is in this temorary directory");
            System.out.print("and input the file name (up to 32 symbols): ");
            String fileName = buffer.readLine();
            System.out.println(fileName);
            
            System.out.println("Bedankt, ff wachten .. ");
            
            String fileNameDummied = "Dummied"+fileName;
            String fileNameUnDummied ="Nulled"+fileName;
            
            CMDComponentSetFilenamesToNullTestHelper helper = new CMDComponentSetFilenamesToNullTestHelper();
            
            helper.setFileNamesToNullInFile(dirName, fileName, fileNameDummied, fileNameUnDummied);
            
            System.out.println("Now look up the directory target/"+dirName);
            
            
    }
     
    
}
