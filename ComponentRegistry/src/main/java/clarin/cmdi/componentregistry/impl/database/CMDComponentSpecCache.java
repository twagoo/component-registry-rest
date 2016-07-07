package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.components.ComponentSpec;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections.map.LRUMap;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CMDComponentSpecCache implements Map<String, ComponentSpec> {

    private Map<String, ComponentSpec> specCacheMap;

    public CMDComponentSpecCache() {
	specCacheMap = Collections.synchronizedMap(new LRUMap(100));
    }

    @Override
    public int size() {
	return specCacheMap.size();
    }

    @Override
    public boolean isEmpty() {
	return specCacheMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
	return specCacheMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
	return specCacheMap.containsKey(value);
    }

    @Override
    public ComponentSpec get(Object key) {
	return specCacheMap.get(key);
    }

    @Override
    public ComponentSpec put(String key, ComponentSpec value) {
	return specCacheMap.put(key, value);
    }

    @Override
    public ComponentSpec remove(Object key) {
	return specCacheMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends ComponentSpec> m) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<String> keySet() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<ComponentSpec> values() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entry<String, ComponentSpec>> entrySet() {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
