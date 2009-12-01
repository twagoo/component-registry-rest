package clarin.cmdi.componentregistry.rest;

import org.apache.commons.lang.StringUtils;

import clarin.cmdi.componentregistry.model.AbstractDescription;

public class DescriptionValidator implements Validator {

    private final AbstractDescription desc;
    private String errorMessage;

    public DescriptionValidator(AbstractDescription desc) {
        this.desc = desc;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean validate() {
        if (!isOk(desc.getCreatorName(), desc.getDescription(), desc.getName())) {
            errorMessage = "Fields are not filled in correctly";
        }
        return errorMessage == null;
    }

    private boolean isOk(String... fields) {
        boolean isOk = true;
        for (String field : fields) {
            isOk &= StringUtils.isNotBlank(field);
        }
        return isOk;
    }

}
