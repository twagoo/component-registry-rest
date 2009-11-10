package clarin.cmdi.componentregistry;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;

public class MDValidator {

    private String errorMessage;

    public CMDComponentSpec validateInputStream(InputStream input) {
        try {
            CMDComponentSpec spec = MDMarshaller.unmarshal(CMDComponentSpec.class, input, MDMarshaller.getCMDComponentSchema());
            return spec;
        } catch (JAXBException e) {
            errorMessage = "Error in validation input file. Error is: " + e;
        }
        return null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
