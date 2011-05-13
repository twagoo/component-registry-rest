package clarin.cmdi.componentregistry.impl.database;

/**
 * Interface for a simple service factory (with ComponentRegistry implementation
 * being the service), required for injecting the dao's into the
 * component registry instances.
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ComponentRegistryBeanFactory {

    public ComponentRegistryDbImpl getNewComponentRegistry();
}
