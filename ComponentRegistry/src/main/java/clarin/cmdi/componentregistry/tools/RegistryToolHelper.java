package clarin.cmdi.componentregistry.tools;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.model.RegisterResponse;
import clarin.cmdi.componentregistry.rest.ComponentRegistryRestService;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.multipart.FormDataMultiPart;

public class RegistryToolHelper {

    private final static Logger LOG = LoggerFactory.getLogger(RegistryToolHelper.class);

    private final WebResource service;
    private int failed = 0;

    private final String userName;

    private final String password;

    public RegistryToolHelper(WebResource service, String userName, String password) {
        this.service = service;
        this.userName = userName;
        this.password = password;
    }

    public void registerComponent(InputStream input, String creatorName, String description, String group, String name) throws IOException {
        FormDataMultiPart form = createForm(input, creatorName, description, name);
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, group);
        RegisterResponse response = getAuthenticatedResource("/components").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        handleResult(response);
    }

    public void registerProfile(InputStream input, String creatorName, String description, String name) throws IOException {
        FormDataMultiPart form = createForm(input, creatorName, description, name);
        RegisterResponse response = getAuthenticatedResource("/profiles").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        handleResult(response);
    }
    
    private Builder getAuthenticatedResource(String path) {
        return service.path(path).header(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encode(userName+":"+password)));
    }


    private FormDataMultiPart createForm(InputStream input, String creatorName, String description, String name) throws IOException {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, input, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, name);
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, description);
        return form;
    }

    private void handleResult(RegisterResponse response) {
        if (response.isRegistered()) {
            LOG.info("Registration ok.");
        } else {
            failed++;
            LOG.error("Registration failed with the following errors:");
            for (int i = 0; i < response.getErrors().size(); i++) {
                LOG.error(response.getErrors().get(i));
            }
        }
    }

    public int getNrOfFailed() {
        return failed;
    }

}
