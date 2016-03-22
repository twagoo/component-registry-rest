package clarin.cmdi.componentregistry;

import java.io.InputStream;
import java.io.Writer;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ComponentSpecConverter {
    
    void convertComponentSpec(CmdVersion sourceVersion, CmdVersion targetVersion, InputStream stream, Writer writer);
    
}
