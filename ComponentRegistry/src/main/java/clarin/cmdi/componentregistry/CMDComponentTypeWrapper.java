/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import java.util.List;

/**
 *
 * @author olhsha
 */
public class CMDComponentTypeWrapper { // extending CMDComponentType does not work :( does not want to downcast
    
    CMDComponentType component;
    List<CMDComponentType> listofcomponents;
    
    public CMDComponentTypeWrapper(CMDComponentType newcomponent){
        component = newcomponent;
        listofcomponents = newcomponent.getCMDComponent();
    }
    
    
    public CMDComponentTypeWrapper(CMDComponentSpec newspec){
        component = null;
        listofcomponents = newspec.getCMDComponent();
    }
    
    
    public void setFileNamesToNull(){ 
   
        if (component != null) {component.setFilename(null);}
        
        setFileNamesFromListToNull(listofcomponents);
    
    }
    
    
    private void setFileNamesFromListToNull(List<CMDComponentType> listofcomponents){ 
     
        for (CMDComponentType currentcomponent : listofcomponents) {
                setFileNamesToNullCurrent(currentcomponent);
                } 
    
    }
    
    private void setFileNamesToNullCurrent(CMDComponentType currentcomponent){ 
     
      currentcomponent.setFilename(null);
      setFileNamesFromListToNull(currentcomponent.getCMDComponent());
    
    }
    
    
    
}
