package finalconfigclasses.cfg.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a single {@code &lt;config-class&gt;} element inside the
 * config-classes description XML (see config-classes.xsd): the blueprint the
 * code generator uses to emit one generated config class (and, optionally,
 * its matching DiffHelper class).
 *
 * Plain POJO, replacing the old JAXB-generated class - see the note in
 * {@link Attribute} for background.
 */
public class ConfigClass {

	private List<Attribute> attribute;
	private List<Property> property;
	private String _extends;
	private String key;
	private String name;
	private String _package;

	public ConfigClass() {
	}

	/**
	 * Returns a reference to the live list, not a snapshot. Any modification
	 * you make to the returned list is reflected inside this object (mirrors
	 * the original JAXB accessor semantics, so the Velocity templates and
	 * generator code that rely on this behavior keep working unchanged).
	 */
	public List<Attribute> getAttribute() {
		if (attribute == null) {
			attribute = new ArrayList<Attribute>();
		}
		return this.attribute;
	}

	public List<Property> getProperty() {
		if (property == null) {
			property = new ArrayList<Property>();
		}
		return this.property;
	}

	public String getExtends() {
		if (_extends == null) {
			return "BaseConfigBean";
		} else {
			return _extends;
		}
	}

	public void setExtends(String value) {
		this._extends = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String value) {
		this.key = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getPackage() {
		return _package;
	}

	public void setPackage(String value) {
		this._package = value;
	}

	@Override
	public String toString() {
		return "ConfigClass[name=" + name + ", package=" + _package + ", key=" + key + ", extends=" + getExtends() + "]";
	}
}
