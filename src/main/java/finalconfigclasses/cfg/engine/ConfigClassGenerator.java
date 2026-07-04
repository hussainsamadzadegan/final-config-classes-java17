package finalconfigclasses.cfg.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

import finalconfigclasses.cfg.model.Attribute;
import finalconfigclasses.cfg.model.ConfigClass;
import finalconfigclasses.cfg.model.ConfigClasses;
import finalconfigclasses.cfg.model.ConfigModelIO;

/**
 * Generates the config bean source files (and, optionally, their matching
 * DiffHelper classes) described by a config-classes description XML (see
 * config-classes.xsd), by merging the bundled Velocity templates
 * (confclass-template.vm / diffhelper-template.vm / array-template.vm /
 * flexible-lodsav-template.vm / rigid-lodsav-template.vm) against the parsed
 * description model.
 *
 * This is a direct replacement for the original {@code CfgGenTask} Ant task.
 * The generation logic (attribute-inheritance resolution, .NET-style
 * nullable-type mapping, destination path computation, etc.) is unchanged;
 * what changed is:
 * <ul>
 *   <li>No dependency on Apache Ant - this is a plain, directly callable
 *       Java class (see {@link #generate(Options)} and {@link #main(String[])}),
 *       used both from the command line and from the Swing generator UI
 *       ({@link finalconfigclasses.cfg.engine.ui.ConfigGeneratorApp}).</li>
 *   <li>No dependency on JAXB (javax.xml.bind, removed from the JDK since
 *       Java 11) to parse the description XML - see {@link ConfigModelIO},
 *       which uses XStream instead.</li>
 *   <li>The legacy "codebeautiful"/Jacobe post-formatting step has been
 *       dropped - Jacobe is an unmaintained Windows-only external tool with
 *       no Java 17-era equivalent bundled here. If you still rely on it,
 *       run it as a separate step over the generated sources.</li>
 * </ul>
 */
public final class ConfigClassGenerator {

	/** Generation options; mirrors the old Ant task's setter-based attributes. */
	public static final class Options {
		public String xmlFile;
		public String destDir;
		public boolean generateDiffHelpers = true;
		/**
		 * true ("flexible"): generated load()/save() auto-create the
		 * ZooKeeper znode path if it does not exist yet.
		 * false ("rigid"): generated load()/save() require the znode to
		 * already exist, and fail otherwise.
		 */
		public boolean manageConfigXml = true;
		/**
		 * Optional directory containing custom/overridden *.vm templates. If
		 * null, the templates bundled on the classpath (inside this jar) are
		 * used.
		 */
		public String templatesDir;

		public Options() {
		}

		public Options(String xmlFile, String destDir) {
			this.xmlFile = xmlFile;
			this.destDir = destDir;
		}
	}

	private final PrintStream log;

	public ConfigClassGenerator() {
		this(System.out);
	}

	public ConfigClassGenerator(PrintStream log) {
		this.log = log != null ? log : System.out;
	}

	public void generate(Options opts) throws Exception {
		if (opts.destDir == null || opts.destDir.length() == 0) {
			throw new IllegalArgumentException("No destination directory is set.");
		}
		if (opts.xmlFile == null || opts.xmlFile.length() == 0) {
			throw new IllegalArgumentException("No xml file is set.");
		}
		log.println("Description XML : " + opts.xmlFile);
		log.println("Destination dir : " + opts.destDir);

		VelocityEngine ve = buildVelocityEngine(opts.templatesDir);

		ConfigClasses confClasses = ConfigModelIO.read(opts.xmlFile);

		convertDotNetTypesToJavaTypes(confClasses);

		HashMap<String, ConfigClass> configClassMap = getConfigClassMap(confClasses);

		for (ConfigClass configClass : confClasses.getConfigClass()) {
			log.println("Generating " + configClass.getPackage() + "." + configClass.getName() + " ...");

			Template t = ve.getTemplate("confclass-template.vm");
			VelocityContext context = new VelocityContext();

			// list of attributes for load/save methods (includes inherited attributes)
			ArrayList<Attribute> loadSaveAttrList = getInheritanceAttr(configClassMap, configClass);

			context.put("configClass", configClass);
			context.put("loadSaveAttrList", loadSaveAttrList);
			context.put("Utils", new Utils());
			context.put("generateDiffHelpers", Boolean.valueOf(opts.generateDiffHelpers));
			context.put("manageConfigXml", Boolean.valueOf(opts.manageConfigXml));

			String destpath = getDestPath(opts.destDir, configClass.getPackage());
			File f = new File(destpath);
			f.mkdirs();
			String fileName = destpath + "/" + configClass.getName() + ".java";
			try (Writer writer = new FileWriter(fileName)) {
				t.merge(context, writer);
			}

			if (opts.generateDiffHelpers) {
				Template difft = ve.getTemplate("diffhelper-template.vm");
				String diffFileName = destpath + "/" + configClass.getName() + "DiffHelper.java";
				try (Writer diffWriter = new FileWriter(diffFileName)) {
					difft.merge(context, diffWriter);
				}
			}
		}

		log.println("Done.");
	}

	private static VelocityEngine buildVelocityEngine(String templatesDir) {
		Properties props = new Properties();
		if (templatesDir != null && templatesDir.length() > 0) {
			props.put("resource.loaders", "file");
			props.put("resource.loader.file.class", FileResourceLoader.class.getName());
			props.put("resource.loader.file.path", templatesDir);
		} else {
			props.put("resource.loaders", "classpath");
			props.put("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
		}
		VelocityEngine ve = new VelocityEngine();
		ve.init(props);
		return ve;
	}

	private static void convertDotNetTypesToJavaTypes(ConfigClasses confClasses) {
		for (ConfigClass configClass : confClasses.getConfigClass()) {
			List<Attribute> attrList = configClass.getAttribute();
			for (Attribute attr : attrList) {
				String type = attr.getType();
				if ("bool".equals(type)) {
					attr.setType("boolean");
				} else if ("bool?".equals(type)) {
					attr.setType("Boolean");
				} else if ("char?".equals(type)) {
					attr.setType("Character");
				} else if ("short?".equals(type)) {
					attr.setType("Short");
				} else if ("int?".equals(type)) {
					attr.setType("Integer");
				} else if ("long?".equals(type)) {
					attr.setType("Long");
				} else if ("float?".equals(type)) {
					attr.setType("Float");
				} else if ("double?".equals(type)) {
					attr.setType("Double");
				}
			}
		}
	}

	private static String getDestPath(String destdir, String pck) {
		StringBuilder sb = new StringBuilder(destdir);
		sb.append('/');

		String[] strArr = pck.split("[.]");
		for (int i = 0; i < strArr.length; i++) {
			sb.append(strArr[i]);
			if (i != strArr.length - 1)
				sb.append('/');
		}
		return sb.toString();
	}

	private static HashMap<String, ConfigClass> getConfigClassMap(ConfigClasses confClasses) throws Exception {
		HashMap<String, ConfigClass> map = new HashMap<String, ConfigClass>();

		for (ConfigClass configClass : confClasses.getConfigClass()) {
			String fullyQualifiedName = "" + configClass.getPackage() + "." + configClass.getName();
			map.put(fullyQualifiedName, configClass);
		}

		return map;
	}

	private static ArrayList<Attribute> getInheritanceAttr(HashMap<String, ConfigClass> configClassMap,
			ConfigClass configClass) throws Exception {
		ArrayList<Attribute> result = new ArrayList<Attribute>();

		ConfigClass tmp = configClass;
		String baseClass = null;

		while (tmp != null) {
			for (int i = tmp.getAttribute().size() - 1; i > -1; i--)
				result.add(tmp.getAttribute().get(i));

			baseClass = tmp.getExtends();

			if ("BaseConfigBean".equals(baseClass))
				break;

			baseClass = fullyQualified(baseClass, tmp.getPackage());
			tmp = configClassMap.get(baseClass);
		}

		Collections.reverse(result);

		return result;
	}

	private static String fullyQualified(String baseClass, String pkg) {
		if (baseClass.indexOf('.') == -1)
			return pkg + "." + baseClass;
		return baseClass;
	}

	/**
	 * Command line entry point.
	 *
	 * <pre>
	 * java -cp configclasses-framework.jar finalconfigclasses.cfg.engine.ConfigClassGenerator \
	 *     --xml config-classes.xml --dest src/main/java [--no-diffhelpers] [--rigid] [--templates dir]
	 * </pre>
	 */
	public static void main(String[] args) throws Exception {
		Options opts = new Options();
		for (int i = 0; i < args.length; i++) {
			String a = args[i];
			if ("--xml".equals(a)) {
				opts.xmlFile = args[++i];
			} else if ("--dest".equals(a)) {
				opts.destDir = args[++i];
			} else if ("--no-diffhelpers".equals(a)) {
				opts.generateDiffHelpers = false;
			} else if ("--rigid".equals(a)) {
				opts.manageConfigXml = false;
			} else if ("--templates".equals(a)) {
				opts.templatesDir = args[++i];
			} else if ("--help".equals(a) || "-h".equals(a)) {
				printUsage();
				return;
			} else {
				System.err.println("Unknown argument: " + a);
				printUsage();
				System.exit(1);
			}
		}
		if (opts.xmlFile == null || opts.destDir == null) {
			printUsage();
			System.exit(1);
			return;
		}
		new ConfigClassGenerator().generate(opts);
	}

	private static void printUsage() {
		System.out.println("Usage: ConfigClassGenerator --xml <config-classes.xml> --dest <output dir> "
				+ "[--no-diffhelpers] [--rigid] [--templates <dir>]");
	}
}
