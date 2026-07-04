package finalconfigclasses.cfg.zk;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An in-memory snapshot of the "attribute" (simple, non-relation) fields of
 * exactly one config bean, together with a flag per attribute recording
 * whether it has been explicitly set (as opposed to still holding its
 * default value).
 *
 * A snapshot is what gets serialized (via XStream, see {@link ZkConfigStore})
 * into the data payload of a single ZooKeeper znode - one znode per config
 * bean instance, addressed by that bean's {@code _getXPath()}. This replaces
 * the original per-attribute-XML-element-inside-a-shared-file model with a
 * single atomic read/write per bean, which maps far more naturally onto
 * ZooKeeper's znode-data model (and is cheaper: one round trip instead of
 * one per attribute).
 */
public class ZkAttrSnapshot implements Serializable {

	private static final long serialVersionUID = 1L;

	private LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
	private LinkedHashMap<String, Boolean> setFlags = new LinkedHashMap<String, Boolean>();

	/** Whether this snapshot actually reflects znode data that was read back
	 *  from ZooKeeper (as opposed to being an "empty"/just-created marker). */
	private transient boolean loaded;

	public ZkAttrSnapshot() {
	}

	public ZkAttrSnapshot(Map<String, Object> values, Map<String, Boolean> setFlags) {
		this.values.putAll(values);
		this.setFlags.putAll(setFlags);
		this.loaded = true;
	}

	public boolean isSet(String attrName) {
		Boolean b = setFlags.get(attrName);
		return b != null && b.booleanValue();
	}

	public boolean hasKey(String attrName) {
		return values.containsKey(attrName);
	}

	public Object getValue(String attrName) {
		return values.get(attrName);
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public Map<String, Boolean> getSetFlags() {
		return setFlags;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
}
