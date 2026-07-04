package finalconfigclasses.cfg.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Reads and writes the config-classes description XML (config-classes.xml,
 * validated against config-classes.xsd).
 *
 * Historically this XML document was parsed with JAXB
 * (javax.xml.bind.JAXBContext / Unmarshaller). javax.xml.bind was removed
 * from the JDK in Java 11 and is therefore not usable on Java 17 without
 * pulling in the (now unmaintained) standalone JAXB artifacts. Since XStream
 * is already a dependency of this framework (see {@link finalconfigclasses.cfg.Registry}),
 * this class simply reuses it - satisfying both "drop JAXB" and "use the
 * latest XStream" in one step - to map the description XML directly onto the
 * plain {@link ConfigClasses}/{@link ConfigClass}/{@link Attribute}/
 * {@link Property} POJOs, preserving the exact same XML shape that was
 * produced/consumed by the original JAXB binding.
 */
public final class ConfigModelIO {

	private static final XStream XSTREAM = createXStream();

	private ConfigModelIO() {
	}

	public static ConfigClasses read(String xmlFile) throws Exception {
		return read(new File(xmlFile));
	}

	public static ConfigClasses read(File xmlFile) throws Exception {
		try (InputStream in = new FileInputStream(xmlFile)) {
			return read(in);
		}
	}

	public static ConfigClasses read(InputStream in) throws Exception {
		String xml = readFully(in);
		String cleaned = stripSchemaNoise(xml);
		Object obj = XSTREAM.fromXML(cleaned);
		if (!(obj instanceof ConfigClasses)) {
			throw new IllegalArgumentException(
					"Root element of the description XML is not <config-classes>.");
		}
		return (ConfigClasses) obj;
	}

	private static String readFully(InputStream in) throws Exception {
		java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
		byte[] chunk = new byte[8192];
		int n;
		while ((n = in.read(chunk)) != -1) {
			buf.write(chunk, 0, n);
		}
		return buf.toString(StandardCharsets.UTF_8.name());
	}

	/**
	 * The description XML's root {@code <config-classes>} element carries
	 * {@code xmlns}/{@code xmlns:xsi}/{@code xsi:noNamespaceSchemaLocation}
	 * attributes for XSD validation purposes only; they have no
	 * corresponding fields on {@link ConfigClasses} and would otherwise need
	 * XStream to silently tolerate unmapped attributes. Stripping them here
	 * keeps the parsing step self-contained and independent of that
	 * behavior.
	 */
	private static String stripSchemaNoise(String xml) {
		return xml.replaceAll("\\sxmlns(:\\w+)?=\"[^\"]*\"", "")
				.replaceAll("\\sxsi:noNamespaceSchemaLocation=\"[^\"]*\"", "")
				.replaceAll("\\sxsi:schemaLocation=\"[^\"]*\"", "");
	}

	public static void write(ConfigClasses configClasses, String xmlFile) throws Exception {
		write(configClasses, new File(xmlFile));
	}

	public static void write(ConfigClasses configClasses, File xmlFile) throws Exception {
		try (OutputStream out = new FileOutputStream(xmlFile)) {
			write(configClasses, out);
		}
	}

	public static void write(ConfigClasses configClasses, OutputStream out) throws Exception {
		XSTREAM.toXML(configClasses, out);
	}

	private static XStream createXStream() {
		XStream xs = new XStream(new StaxDriver());
		XStream.setupDefaultSecurity(xs);
		xs.allowTypesByWildcard(new String[] { "finalconfigclasses.cfg.model.**" });

		xs.alias("config-classes", ConfigClasses.class);
		xs.alias("config-class", ConfigClass.class);
		xs.alias("attribute", Attribute.class);
		xs.alias("property", Property.class);

		// no wrapper element between <config-classes> and its <config-class> children
		xs.addImplicitCollection(ConfigClasses.class, "configClass", "config-class", ConfigClass.class);
		xs.addImplicitCollection(ConfigClass.class, "attribute", "attribute", Attribute.class);
		xs.addImplicitCollection(ConfigClass.class, "property", "property", Property.class);

		xs.aliasField("package", ConfigClass.class, "_package");
		xs.aliasField("extends", ConfigClass.class, "_extends");
		xs.useAttributeFor(ConfigClass.class, "name");
		xs.useAttributeFor(ConfigClass.class, "_package");
		xs.useAttributeFor(ConfigClass.class, "key");
		xs.useAttributeFor(ConfigClass.class, "_extends");

		xs.useAttributeFor(Attribute.class, "name");
		xs.useAttributeFor(Attribute.class, "type");
		xs.useAttributeFor(Attribute.class, "key");
		xs.useAttributeFor(Attribute.class, "isArray");

		xs.useAttributeFor(Property.class, "name");
		xs.useAttributeFor(Property.class, "type");
		xs.useAttributeFor(Property.class, "isArray");

		return xs;
	}
}
