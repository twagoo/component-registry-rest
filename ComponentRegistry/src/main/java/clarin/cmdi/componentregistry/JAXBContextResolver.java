/*
 * Copyright (C) 2020 CLARIN ERIC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.components.ComponentType;
import clarin.cmdi.componentregistry.components.ElementType;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Component
@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {

    private final JAXBContext context;
    private final Class[] types = {ComponentSpec.class, ComponentType.class, ElementType.class};

    public JAXBContextResolver() throws Exception {
        JSONConfiguration jsonConfiguration = jsonConfiguration();

        this.context = new JSONJAXBContext(jsonConfiguration, types);
    }

    @Override
    public JAXBContext getContext(Class<?> objectType) {
        for (Class type : types) {
            if (type == objectType) {
                return context;
            }
        }
        return null;
    }

    public final static JSONConfiguration jsonConfiguration() {
        final ImmutableMap<String, String> namespacesMap = ImmutableMap.<String, String>builder()
                .put("http://www.clarin.eu/cmdi/cues/1", "cue")
                .build();
        final JSONConfiguration jsonConfiguration = JSONConfiguration.mappedJettison()
                .xml2JsonNs(namespacesMap)
                .build();
        return jsonConfiguration;
    }
}
