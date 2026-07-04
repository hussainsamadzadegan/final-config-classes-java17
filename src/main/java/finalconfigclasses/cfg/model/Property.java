package finalconfigclasses.cfg.model;

/**
 * Describes a single relation ("property") field of a generated config
 * class, as declared by a {@code &lt;property&gt;} element inside the
 * config-classes description XML (see config-classes.xsd). A property
 * references another (nested) config class rather than holding a simple
 * value.
 *
 * Plain POJO, replacing the old JAXB-generated class - see the note in
 * {@link Attribute} for background.
 */
public class Property {

	private String isArray;
	private String name;
	private String type;

	public Property() {
	}

	public Property(String name, String type, String isArray) {
		this.name = name;
		this.type = type;
		this.isArray = isArray;
	}

	public String getIsArray() {
		if (isArray == null) {
			return "false";
		} else {
			return isArray;
		}
	}

	public void setIsArray(String value) {
		this.isArray = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String value) {
		this.type = value;
	}

	@Override
	public String toString() {
		return "Property[name=" + name + ", type=" + type + ", isArray=" + getIsArray() + "]";
	}
}
