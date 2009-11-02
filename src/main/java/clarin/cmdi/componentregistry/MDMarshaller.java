package clarin.cmdi.componentregistry;

import java.io.InputStream;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;


public class MDMarshaller {

    private MDMarshaller() {
    }

    public static <T> T unmarshal(Class<T> docClass, InputStream inputStream) throws JAXBException {
        String packageName = docClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller u = jc.createUnmarshaller();
        Object unmarshal = u.unmarshal(inputStream);
        T doc = (T) unmarshal;
        return doc;
    }

    public static void marshall(CMDComponentSpec myProfile, Writer writer) throws JAXBException {
        String packageName = myProfile.getClass().getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(myProfile, writer);
    }
    
    public static <T> void marshall(T marshallableObject, Writer writer) throws JAXBException {
        String packageName = marshallableObject.getClass().getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(marshallableObject, writer);
    }

}
