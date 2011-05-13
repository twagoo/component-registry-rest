package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentRegistryDbImpl implements ComponentRegistry {

    private String user;
    private ProfileDescriptionDao profileDescriptionDao;
    private ComponentDescriptionDao componentDescriptionDao;

    /**
     * Creates a new ComponentRegistry (either public or not) for the provided user
     * @param userSpace Whether the registry is public
     * @param user Username of the user to create registry for
     */
    public ComponentRegistryDbImpl(String user) {
        this.user = user;

        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-database-impl.xml");
        componentDescriptionDao = (ComponentDescriptionDao) context.getBean("componentDescriptionDao");
        profileDescriptionDao = (ProfileDescriptionDao) context.getBean("profileDescriptionDao");
    }

    @Override
    public List<ProfileDescription> getProfileDescriptions() {
        if (isPublic()) {
            return profileDescriptionDao.getPublicProfileDescriptions();
        } else {
            return null;
        }
    }

    @Override
    public ProfileDescription getProfileDescription(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ComponentDescription> getComponentDescriptions() {
        if (isPublic()) {
            return componentDescriptionDao.getPublicComponentDescriptions();
        } else {
            return null;
        }
    }

    @Override
    public ComponentDescription getComponentDescription(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CMDComponentSpec getMDProfile(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CMDComponentSpec getMDComponent(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int register(AbstractDescription desc, CMDComponentSpec spec) {
        try {
            OutputStream os = new ByteArrayOutputStream();
            MDMarshaller.marshal(spec, os);
            String xml = os.toString();
            if (!desc.isProfile()) {
                componentDescriptionDao.insertComponent(desc, xml);
            }
            return 0;
        } catch (JAXBException ex) {
            Logger.getLogger(ComponentRegistryDbImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ComponentRegistryDbImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int update(AbstractDescription description, CMDComponentSpec spec) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int publish(AbstractDescription desc, CMDComponentSpec spec, Principal principal) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getMDProfileAsXml(String profileId, OutputStream output) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getMDProfileAsXsd(String profileId, OutputStream outputStream) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getMDComponentAsXml(String componentId, OutputStream output) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getMDComponentAsXsd(String componentId, OutputStream outputStream) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteMDProfile(String profileId, Principal principal) throws IOException, UserUnauthorizedException, DeleteFailedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteMDComponent(String componentId, Principal principal, boolean forceDelete) throws IOException, UserUnauthorizedException, DeleteFailedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ComponentDescription> getUsageInComponents(String componentId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ProfileDescription> getUsageInProfiles(String componentId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isPublic() {
        return null == user;
    }
}
