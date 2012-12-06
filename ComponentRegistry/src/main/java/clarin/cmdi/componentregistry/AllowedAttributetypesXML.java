/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.AllowedAttributetypesType;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author olhsha
 */
@XmlRootElement(name = "CMDElement_Types")
public class AllowedAttributetypesXML {
    
    @XmlElement(name="elementType")
    List<String> _elementTypes;

    public AllowedAttributetypesXML() {
        AllowedAttributetypesType[] elementTypes = AllowedAttributetypesType.values();
        _elementTypes = new ArrayList<String>(); 
        
        for (AllowedAttributetypesType at : elementTypes) {
            _elementTypes.add(at.value());
        };
    }
}
