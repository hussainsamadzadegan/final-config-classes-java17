package finalconfigclasses.cfg.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Root element of the config-classes description XML: a simple list of
 * {@link ConfigClass} declarations.
 *
 * Plain POJO, replacing the old JAXB-generated class - see the note in
 * {@link Attribute} for background.
 */
public class ConfigClasses {

	private List<ConfigClass> configClass;

	public ConfigClasses() {
	}

	public List<ConfigClass> getConfigClass() {
		if (configClass == null) {
			configClass = new ArrayList<ConfigClass>();
		}
		return this.configClass;
	}
}
