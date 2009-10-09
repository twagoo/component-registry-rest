package clarin.cmdi.componentregistry;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public class ComponentRegistryWSImpl {

    @WebMethod
    public List<String> getRegisteredComponentNames() {
        List<String> result = new ArrayList<String>();
        result.add("Test1");
        result.add("Test2");
        return result;
    }
}
