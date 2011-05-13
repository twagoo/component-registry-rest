package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryUtils;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of ComponentRegistry that uses Database Acces Objects for
 * accessing the registry (ergo: a database implementation)
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentRegistryDbImpl implements ComponentRegistry {

    private String user;
    @Autowired
    private ProfileDescriptionDao profileDescriptionDao;
    @Autowired
    private ComponentDescriptionDao componentDescriptionDao;

    /**
     * Default constructor, makes this a (spring) bean. No user is set, so
     * public registry by default. Use setUser() to make it a user registry.
     * @see setUser
     */
    public ComponentRegistryDbImpl() {
    }

    /**
     * Creates a new ComponentRegistry (either public or not) for the provided user
     * @param user Username of the user to create registry for. Pass null for public
     */
    public ComponentRegistryDbImpl(String user) {
        this.user = user;
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
        return getMDComponent(id, profileDescriptionDao);
    }

    @Override
    public CMDComponentSpec getMDComponent(String id) {
        return getMDComponent(id, componentDescriptionDao);
    }

    private CMDComponentSpec getMDComponent(String id, AbstractDescriptionDao dao) {
        String xml = dao.getContent(id);
        if (xml != null) {
            try {
                InputStream is = new ByteArrayInputStream(xml.getBytes());
                return MDMarshaller.unmarshal(CMDComponentSpec.class, is, MDMarshaller.getCMDComponentSchema());
            } catch (JAXBException ex) {
                Logger.getLogger(ComponentRegistryDbImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public int register(AbstractDescription description, CMDComponentSpec spec) {
        ComponentRegistryUtils.enrichSpecHeader(spec, description);
        try {
            OutputStream os = new ByteArrayOutputStream();
            MDMarshaller.marshal(spec, os);
            String xml = os.toString();
            if (!description.isProfile()) {
                componentDescriptionDao.insertComponent(description, xml);
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

    public void setPublic() {
        this.user = null;
    }

    /**
     * @return The user, or null if this is the public registry.
     */
    public String getUser() {
        return user;
    }

    /**
     * @param User for which this should be the registry. Pass null for
     * the public registry
     */
    public void setUser(String user) {
        this.user = user;
    }
}
