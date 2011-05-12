package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentRegistryDbImpl implements ComponentRegistry {

    @Autowired
    private Configuration configuration;

    @Override
    public List<ProfileDescription> getProfileDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ProfileDescription getProfileDescription(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ComponentDescription> getComponentDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private ProfileDescriptionDao profileDescriptionDao;

    public void setProfileDescriptionDao(ProfileDescriptionDaoImpl profileDescriptionDao) {
        this.profileDescriptionDao = profileDescriptionDao;
    }

}
