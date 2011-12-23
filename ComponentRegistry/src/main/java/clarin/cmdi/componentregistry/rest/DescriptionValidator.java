package clarin.cmdi.componentregistry.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import clarin.cmdi.componentregistry.model.AbstractDescription;

public class DescriptionValidator implements Validator {

    private final AbstractDescription desc;
    private List<String> errorMessages = new ArrayList<String>();

    public DescriptionValidator(AbstractDescription desc) {
        this.desc = desc;
    }
    
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public boolean validate() {
        if (!isOk(desc.getCreatorName(), desc.getDescription(), desc.getName())) {
            errorMessages.add("Fields are not filled in correctly");
        }
        return errorMessages.isEmpty();
    }
    
    private boolean isOk(String... fields) {
        boolean isOk = true;
        for (String field : fields) {
            isOk &= StringUtils.isNotBlank(field);
        }
        return isOk;
    }

}
