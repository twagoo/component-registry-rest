package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.ComponentSpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import javax.xml.validation.Schema;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface IMarshaller {

    void generateXsd(ComponentSpec spec, CmdVersion[] cmdVersions, OutputStream outputStream) throws JAXBException, TransformerException;

    Schema getComponentSchema();

    /**
     * Will wrap the Outputstream in a OutputStreamWriter with encoding set to
     * UTF-8. This to make sure profiles are stored correctly.
     * @param <T> object type to marshall from
     * @param marshallableObject object to marshall
     * @param out stream to marshall to
     * @throws javax.xml.bind.JAXBException
     * @throws java.io.UnsupportedEncodingException
     */
    <T> void marshal(T marshallableObject, OutputStream out) throws JAXBException, UnsupportedEncodingException;

    /**
     * @param <T> object type to marshall from
     * @param marshallableObject
     * @return the xml representation of the marshallableObject
     */
    <T> String marshalToString(T marshallableObject);

    /**
     *
     * @param <T> object type to unmarshall to
     * @param docClass
     * @param inputStream
     * @param schema to validate against, can be null for no validation.
     * @return
     * @throws JAXBException
     */
    <T> T unmarshal(Class<T> docClass, InputStream inputStream, Schema schema) throws JAXBException;
    
}
