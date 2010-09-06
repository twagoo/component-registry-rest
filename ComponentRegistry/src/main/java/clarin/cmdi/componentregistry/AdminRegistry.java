package clarin.cmdi.componentregistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.frontend.FileInfo;
import clarin.cmdi.componentregistry.frontend.SubmitFailedException;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public class AdminRegistry {
    private final static Logger LOG = LoggerFactory.getLogger(AdminRegistry.class);

    public void submitFile(FileInfo fileInfo, Principal userPrincipal) throws SubmitFailedException {
        try {
            File file = fileInfo.getFileNode().getFile();
            if ( fileInfo.getFileNode().isDeleted()) {
                //already deleted file
                FileUtils.writeStringToFile(file, fileInfo.getText(), "UTF-8");
            } else {
                //Description or cmdSpec file.
                String name = fileInfo.getName();
                String id = ComponentRegistry.REGISTRY_ID + fileInfo.getFileNode().getFile().getParentFile().getName();
                AbstractDescription originalDescription = getDescription(fileInfo);
                CMDComponentSpec originalSpec = getSpec(fileInfo);
                AbstractDescription description = null;
                CMDComponentSpec spec = null;
                if (ComponentRegistryImpl.DESCRIPTION_FILE_NAME.equals(name)) {
                    if (fileInfo.getFileNode().getFile().getParentFile().getName().startsWith("c_")) {
                        description = MDMarshaller.unmarshal(ComponentDescription.class,
                                IOUtils.toInputStream(fileInfo.getText(), "UTF-8"), null);
                    } else {
                        description = MDMarshaller.unmarshal(ProfileDescription.class, IOUtils.toInputStream(fileInfo.getText(), "UTF-8"),
                                null);
                    }
                    checkId(id, description.getId());
                    spec = originalSpec;
                } else {
                    spec = MDMarshaller.unmarshal(CMDComponentSpec.class, IOUtils.toInputStream(fileInfo.getText(), "UTF-8"), MDMarshaller
                            .getCMDComponentSchema());
                    checkId(id, spec.getHeader().getID());
                    description = originalDescription;
                }
                deleteFromRegistry(userPrincipal, originalDescription, fileInfo);
                int result = submitToRegistry(description, spec, userPrincipal, fileInfo);
                if (result == 0) {
                    //submit is successful so now really delete the old one, we cannot have that around anymore.
                    ComponentRegistryImpl registry = (ComponentRegistryImpl) getRegistry(userPrincipal, originalDescription, fileInfo);
                    registry.emptyFromTrashcan(originalDescription);
                } else {
                    throw new SubmitFailedException("Problem occured while registering, please check the tomcat logs for errors. "
                            + "Original files are removed already you can find them "
                            + "in the deleted section of the registry. You have to put that back manually.");
                }
            }
        } catch (JAXBException e) {
            throw new SubmitFailedException(e);
        } catch (IOException e) {
            throw new SubmitFailedException(e);
        } catch (UserUnauthorizedException e) {
            throw new SubmitFailedException(e);
        } catch (DeleteFailedException e) {
            throw new SubmitFailedException(e);
        }

    }

    private void checkId(String id, String id2) throws SubmitFailedException {
        if (id == null || id2 == null || !id.equals(id2)) {
            throw new SubmitFailedException("Id's do not match up, you cannot edit id's: id1=" + id + ", id2=" + id2);
        }
    }

    public void undelete(FileInfo fileInfo, Principal userPrincipal) throws SubmitFailedException {
        String id = fileInfo.getName();
        AbstractDescription desc = getDescription(fileInfo);
        try {
            CMDComponentSpec spec = getSpec(fileInfo);
            int result = submitToRegistry(desc, spec, userPrincipal, fileInfo);
            if (result == 0) {
                FileUtils.deleteDirectory(fileInfo.getFileNode().getFile());
                LOG.info("Undeleted item: " + id);
            } else {
                throw new SubmitFailedException("Problem occured while registering, please check the tomcat logs for errors.");
            }
        } catch (IOException e) {
            throw new SubmitFailedException(e);
        } catch (JAXBException e) {
            throw new SubmitFailedException(e);
        }

    }

    public void delete(FileInfo fileInfo, Principal userPrincipal) throws SubmitFailedException {
        String id = fileInfo.getName();
        AbstractDescription desc = getDescription(fileInfo);
        try {
            deleteFromRegistry(userPrincipal, desc, fileInfo);
            LOG.info("Deleted item: " + id);
        } catch (IOException e) {
            throw new SubmitFailedException(e);
        } catch (UserUnauthorizedException e) {
            throw new SubmitFailedException(e);
        } catch (DeleteFailedException e) {
            throw new SubmitFailedException(e);
        }

    }

    private int submitToRegistry(AbstractDescription description, CMDComponentSpec spec, Principal userPrincipal, FileInfo fileInfo) {
        ComponentRegistry registry = getRegistry(userPrincipal, description, fileInfo); 
        if (spec.isIsProfile()) {
            return registry.registerMDProfile((ProfileDescription) description, spec);
        } else {
            return registry.registerMDComponent((ComponentDescription) description, spec);
        }

    }

    private void deleteFromRegistry(Principal userPrincipal, AbstractDescription desc, FileInfo fileInfo) throws IOException, UserUnauthorizedException,
            DeleteFailedException {
        ComponentRegistry registry = getRegistry(userPrincipal, desc, fileInfo); 
        LOG.info("Deleting item: " + desc);
        if (desc.isProfile()) {
            registry.deleteMDProfile(desc.getId(), userPrincipal);
        } else {
            registry.deleteMDComponent(desc.getId(), userPrincipal, fileInfo.isForceUpdate());
        }
    }

    private ComponentRegistry getRegistry(Principal userPrincipal, AbstractDescription desc, FileInfo fileInfo) {
        ComponentRegistry registry = ComponentRegistryFactory.getInstance().getPublicRegistry();
        if (fileInfo.isInUserWorkSpace()) {
            registry = ComponentRegistryFactory.getInstance().getOtherUserComponentRegistry(userPrincipal, desc.getUserId());
        }
        return registry;
    }

    private CMDComponentSpec getSpec(FileInfo fileInfo) throws FileNotFoundException, JAXBException {
        File parent = fileInfo.getFileNode().getFile();
        if (!parent.isDirectory()) {
            parent = parent.getParentFile();
        }
        File file = new File(parent, parent.getName() + ".xml");
        CMDComponentSpec spec = MDMarshaller.unmarshal(CMDComponentSpec.class, new FileInputStream(file), MDMarshaller
                .getCMDComponentSchema());
        return spec;
    }

    private AbstractDescription getDescription(FileInfo fileInfo) {
        File parent = fileInfo.getFileNode().getFile();
        if (!parent.isDirectory()) {
            parent = parent.getParentFile();
        }
        File descFile = new File(parent, ComponentRegistryImpl.DESCRIPTION_FILE_NAME);
        Class<? extends AbstractDescription> clazz = fileInfo.isComponent() ? ComponentDescription.class : ProfileDescription.class;
        AbstractDescription result = MDMarshaller.unmarshal(clazz, descFile, null);
        return result;
    }

}
