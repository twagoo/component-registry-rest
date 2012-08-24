/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentType;
import java.util.List;

/**
 *
 * @author olhsha
 */
public class CMDComponentTypeWrapper { // extending CMDComponentType does not work :( does not want to cast
    
     /**
     * Olha: null fileName of *this*, also in children
     *
     */
    
    // may be do this method static? 
    public void setFileNamesToNull(CMDComponentType component){ 
    //public void setFileNamesToNull(){    
        
        // null the filename of component
        component.setFilename(null);
        //filename=null;
                
        // and now null filenames in all children's components
        List<CMDComponentType> cildrencomponents = component.getCMDComponent();
        CMDComponentTypeWrapper childwrapper = new CMDComponentTypeWrapper();
        for (CMDComponentType currentcomponent : cildrencomponents) {
        //for (CMDComponentType currentcomponent : cmdComponent) {
                childwrapper.setFileNamesToNull(currentcomponent);
                //((CMDComponentTypeWrapper) currentcomponent).setFileNamesToNull();
                } 
    
    }
    
    
    
}
