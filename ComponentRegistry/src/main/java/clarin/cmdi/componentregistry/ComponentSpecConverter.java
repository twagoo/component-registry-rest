package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.ComponentSpec;
import java.io.Writer;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ComponentSpecConverter {
    
    void convertComponentSpec(CmdVersion sourceVersion, CmdVersion targetVersion, ComponentSpec spec, Writer writer);
    
}
