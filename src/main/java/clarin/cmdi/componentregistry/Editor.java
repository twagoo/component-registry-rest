package clarin.cmdi.componentregistry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.components.ObjectFactory;

public class Editor {

    public String createProfile(CMDComponentType... cmdComponentType) throws JAXBException, UnsupportedEncodingException {
        CMDComponentSpec locComponent = MDMarshaller.unmarshal(CMDComponentSpec.class, new File(
                "/Users/patdui/Workspace/Clarin/metadata/toolkit/components/imdi/component-location.xml"));
        CMDComponentSpec writtenResourceComponent = MDMarshaller.unmarshal(CMDComponentSpec.class, new File(
                "/Users/patdui/Workspace/Clarin/metadata/toolkit/components/imdi/component-written-resource.xml"));

        CMDComponentSpec myProfile = new ObjectFactory().createCMDComponentSpec();
        myProfile.getCMDComponent().addAll(locComponent.getCMDComponent());
        myProfile.getCMDComponent().addAll(writtenResourceComponent.getCMDComponent());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MDMarshaller.marshal(myProfile, out);
        return out.toString();
    }

}
