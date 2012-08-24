/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author olhsha
 */

public class CMDComponentTypeWrapperTest {

    
    
    @Test    
    public void setFileNamesToNullTest(){
       
      CMDComponentType component= new CMDComponentType();
      
      
      
      // check on a fresh CMDComponentType
      //CMDComponentTypeWrapper.setFileNamesToNull(component); 
      //assertEquals(component.getFilename(), null);
    }
    
}
