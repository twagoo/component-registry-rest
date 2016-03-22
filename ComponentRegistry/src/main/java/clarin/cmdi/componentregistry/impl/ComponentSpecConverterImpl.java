package clarin.cmdi.componentregistry.impl;

import clarin.cmdi.componentregistry.CmdVersion;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import java.io.Writer;
import clarin.cmdi.componentregistry.ComponentSpecConverter;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@Service("ComponentSpecConverter")
public class ComponentSpecConverterImpl implements ComponentSpecConverter {

    @Override
    public void convertComponentSpec(CmdVersion sourceVersion, CmdVersion targetVersion, ComponentSpec spec, Writer writer) {
        //TODO: implement conversion(s)
    }
    
}
