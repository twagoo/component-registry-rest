package clarin.cmdi.componentregistry.tools;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.model.RegisterResponse;
import clarin.cmdi.componentregistry.rest.ComponentRegistryRestService;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;

public class RegistryToolHelper {

    private final static Logger LOG = LoggerFactory.getLogger(RegistryToolHelper.class);

    private final WebResource service;
    private int failed = 0;

    public RegistryToolHelper(WebResource service) {
        this.service = service;
    }

    public void registerComponent(InputStream input, String creatorName, String description, String group, String name) throws IOException {
        FormDataMultiPart form = createForm(input, creatorName, description, name);
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, group);
        RegisterResponse response = service.path("/components").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        handleResult(response);
    }

    public void registerProfile(InputStream input, String creatorName, String description, String name) throws IOException {
        FormDataMultiPart form = createForm(input, creatorName, description, name);
        RegisterResponse response = service.path("/profiles").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        handleResult(response);
    }

    private FormDataMultiPart createForm(InputStream input, String creatorName, String description, String name) throws IOException {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, input, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, name);
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, description);
        form.field(ComponentRegistryRestService.CREATOR_NAME_FORM_FIELD, creatorName);
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
