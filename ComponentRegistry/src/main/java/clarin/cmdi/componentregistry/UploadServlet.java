package clarin.cmdi.componentregistry;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.model.ProfileDescription;

public class UploadServlet extends HttpServlet {

    private final static Logger LOG = LoggerFactory.getLogger(UploadServlet.class);
    private static final long serialVersionUID = 1L;
    private ComponentRegistry registry;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        registry = ComponentRegistryImpl.getInstance();
        LOG.info("UploadServlet initialized");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        if (isMultipart) {
            List<String> errorFiles = new ArrayList<String>();
            List<FileItem> files = new ArrayList<FileItem>();
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            try {
                List<FileItem> items = upload.parseRequest(req);
                Map<String, String> formFields = new HashMap<String, String>();
                Iterator<FileItem> iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();
                    if (item.isFormField()) {//add filename field
                        formFields.put(item.getFieldName(), item.getString());
                    } else {
                        files.add(item);
                    }
                }
                registerComponents(formFields, files, errorFiles);

            } catch (FileUploadException e) {
                LOG.error("Failed to parse upload form: ", e);
            }
            if (!errorFiles.isEmpty()) {
                LOG.error("Files could not be registered" + errorFiles);
                resp.sendError(HttpServletResponse.SC_OK, "Reguest processed but some files could not be registered.");
            }
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = resp.getWriter();
        writer.append("Upload successful.");

    }

    private void registerComponents(Map<String, String> formFields, List<FileItem> files, List<String> errorFiles) {
        for (FileItem fileItem : files) { //TODO Patrick we only support one file at the moment. Validate that we have one file 
            //Need to have different descriptions for every file if we support multiple file uploads
            //Validate name with spaces
            try {
                ProfileDescription desc = new ProfileDescription();
                desc.setCreatorName(formFields.get("creatorName"));
                desc.setName(formFields.get("name"));
                desc.setRegistrationDate(new Date().toString());
                desc.setDescription(formFields.get("description"));
                String id = "profile_" + System.currentTimeMillis();
                desc.setId(id);
                desc.setXlink("link:" + id);
                registry.registerMDProfile(desc, fileItem.getString());
            } catch (Exception e) {
                LOG.error("Error in writing uploaded file", e);
                errorFiles.add(fileItem.getName());
                //TODO Patrick handle error
            }
        }
    }

}
