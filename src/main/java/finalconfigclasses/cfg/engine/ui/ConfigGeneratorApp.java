package finalconfigclasses.cfg.engine.ui;

/**
 * Entry point for the Swing config class generator application. Packaged as
 * the {@code Main-Class} of the "generator" jar built by the Maven build
 * (see pom.xml, maven-shade-plugin) and launched by generate-config-classes.cmd.
 *
 * Usage: {@code java -jar configclasses-framework-<version>-generator.jar [path-to-config-classes.xml]}
 */
public final class ConfigGeneratorApp {

	private ConfigGeneratorApp() {
	}

	public static void main(String[] args) {
		String initialXmlFile = args.length > 0 ? args[0] : null;
		ConfigGeneratorFrame.launch(initialXmlFile);
	}
}
