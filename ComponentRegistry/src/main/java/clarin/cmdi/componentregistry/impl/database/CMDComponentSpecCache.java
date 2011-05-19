package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
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
public class CMDComponentSpecCache implements Map<String, CMDComponentSpec> {

    private Map<String, CMDComponentSpec> specCacheMap;

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
    public CMDComponentSpec get(Object key) {
	return specCacheMap.get(key);
    }

    @Override
    public CMDComponentSpec put(String key, CMDComponentSpec value) {
	return specCacheMap.put(key, value);
    }

    @Override
    public CMDComponentSpec remove(Object key) {
	return specCacheMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends CMDComponentSpec> m) {
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
    public Collection<CMDComponentSpec> values() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entry<String, CMDComponentSpec>> entrySet() {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
