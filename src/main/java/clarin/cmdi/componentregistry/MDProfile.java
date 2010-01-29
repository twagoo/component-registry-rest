package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;

public class MDProfile {
    
    
    private final CMDComponentSpec spec;

    MDProfile(CMDComponentSpec spec) {
        this.spec = spec;
    }
    
    public String getName() {
        return "";
    }
    
    public CMDComponentSpec getCMDComponentSpec() {
        return spec;
    } 

}
