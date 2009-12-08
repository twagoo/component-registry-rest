package clarin.cmdi.componentregistry.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.model.RegisterResponse;
import clarin.cmdi.componentregistry.rest.ComponentRegistryRestService;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;

public class RegistryFiller {

    private final static Logger LOG = LoggerFactory.getLogger(RegistryFiller.class);

    private WebResource service;

    private static int failed = 0;

    RegistryFiller() {
        URI uri = UriBuilder.fromUri("http://localhost/").port(8080).path("ComponentRegistry/rest/registry").build();
        Client client = Client.create();
        service = client.resource(uri);
    }

    /**
     * RegistryFiller "P.Duin" "Test files" imdi -c /Users/patdui/Workspace/Clarin/metadata/toolkit/components/imdi/component*.xml
     * @param args
     */
    public static void main(String[] args) {
        LOG.info("RegistryFiller started with arguments: " + Arrays.toString(args));

        RegistryFiller filler = new RegistryFiller();
        String creatorName = args[0];
        String description = args[1];
        String group = args[2];
        boolean registerProfiles = "-p".equals(args[3]); //Otherwise -c
        for (int i = 4; i < args.length; i++) {
            File file = new File(args[i]);
            LOG.info("Registering " + (i - 3) + "/" + (args.length - 4) + ": " + file.getName());
            try {
                if (registerProfiles) {
                    filler.registerProfile(file, creatorName, description);
                } else {
                    filler.registerComponent(file, creatorName, description, group);
                }
            } catch (IOException e) {
                failed++;
                LOG.error("Error in file: " + file, e);
            }
        }
        if (failed > 0) {
            LOG.error("Failed to register " + failed + " components/profiles.");
        } else {
            LOG.info("Everything registered ok.");
        }
    }

    private void registerProfile(File file, String creatorName, String description) throws IOException {
        FormDataMultiPart form = createForm(file, creatorName, description);
        RegisterResponse response = service.path("/profiles").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        handleResult(response);
    }

    private FormDataMultiPart createForm(File file, String creatorName, String description) throws IOException {
        FormDataMultiPart form = new FormDataMultiPart();
        Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8"); //To handle unicode chars.
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, new ByteArrayInputStream(IOUtils.toByteArray(reader, "UTF-8")),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, FilenameUtils.getBaseName(file.getName()));
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, description);
        form.field(ComponentRegistryRestService.CREATOR_NAME_FORM_FIELD, creatorName);
        return form;
    }

    private void registerComponent(File file, String creatorName, String description, String group) throws IOException {
        FormDataMultiPart form = createForm(file, creatorName, description);
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, group);
        RegisterResponse response = service.path("/components").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        handleResult(response);
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

}
