package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentType;

public class MDComponent {

    private final CMDComponentType cmdComponentType;

    MDComponent(CMDComponentType cmdComponentType) {
        this.cmdComponentType = cmdComponentType;
    }
    
    public CMDComponentType getCmdComponentType() {
        return cmdComponentType;
    }

}
