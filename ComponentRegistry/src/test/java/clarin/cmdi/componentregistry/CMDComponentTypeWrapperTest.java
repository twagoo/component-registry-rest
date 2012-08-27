/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import java.util.List;
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
    private void checkNullnessOfFilenames(CMDComponentType component){
        
       
      assertEquals(component.getFilename(), null);
        
      List<CMDComponentType> childcomponents = component.getCMDComponent();
      
      for (CMDComponentType currentcomponent : childcomponents){
          checkNullnessOfFilenames(currentcomponent);
      }
     
     
    }
    
    
    ///////////////////////////////
    // true if there at least one non-null filename in the given component or its children
    private boolean checkNonNullnessOfFilenames(CMDComponentType component){
        
      boolean check=(component.getFilename() != null);  
      
      if (check) return true;
        
      List<CMDComponentType> childcomponents = component.getCMDComponent();
      
      for (CMDComponentType currentcomponent : childcomponents){
          check=checkNonNullnessOfFilenames(currentcomponent);
          if(check) return true;
      }
     
     return false;
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
      assertTrue(checkNonNullnessOfFilenames(component));
      
      CMDComponentTypeWrapper wrapper = new CMDComponentTypeWrapper(component);
      wrapper.setFileNamesToNull();
      checkNullnessOfFilenames(component);
      
      
      
     
      
      
      // TODO: run "setfilenamesToNull" on files!
      
      
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
      
      for (CMDComponentType currentcomponent : listofcomponents){
          checkNullnessOfFilenames(currentcomponent);
      }
      
      // test on files
    }
    
    
    
    
}
