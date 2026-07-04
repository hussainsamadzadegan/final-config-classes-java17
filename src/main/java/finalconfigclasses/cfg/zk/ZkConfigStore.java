package finalconfigclasses.cfg.zk;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import finalconfigclasses.cfg.ConfigException;

/**
 * Persists/loads using native ZooKeeper client.
 */
public final class ZkConfigStore {

	private static final XStream XSTREAM = createXStream();

	private static final Pattern NAME_ATTR = Pattern.compile("\\[@name='([^']*)'\\]");

	private ZkConfigStore() {
	}

	public static String buildZnodePath(String rootPath, String document, String xpath) {
		// unchanged
		StringBuilder sb = new StringBuilder();
		if (rootPath != null && rootPath.length() > 0) {
			sb.append('/').append(normalizeSegment(rootPath));
		}
		if (document != null && document.length() > 0) {
			sb.append('/').append(normalizeSegment(document));
		}
		if (xpath != null && xpath.length() > 0) {
			sb.append('/').append(sanitizeXPath(xpath));
		}
		String path = sb.toString().replaceAll("/{2,}", "/");
		if (path.length() == 0) {
			path = "/";
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (path.length() > 1 && path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	private static String normalizeSegment(String s) {
		String t = s.trim();
		while (t.startsWith("/")) t = t.substring(1);
		while (t.endsWith("/")) t = t.substring(0, t.length() - 1);
		return t;
	}

	private static String sanitizeXPath(String xpath) {
		Matcher m = NAME_ATTR.matcher(xpath);
		return m.replaceAll("/$1");
	}

	public static ZkAttrSnapshot loadAttributes(ZooKeeper client, String rootPath, String document,
												String xpath, boolean createPathIfAbsent) throws Exception {
		String znodePath = buildZnodePath(rootPath, document, xpath);
		byte[] data;
		try {
			data = client.getData(znodePath, false, null);
		} catch (KeeperException.NoNodeException nne) {
			if (!createPathIfAbsent) {
				throw new ConfigException("ZooKeeper node not found: " + znodePath, nne);
			}
			try {
				client.create(znodePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			} catch (KeeperException.NodeExistsException raceIgnored) {}
			return new ZkAttrSnapshot();
		}
		if (data == null || data.length == 0) {
			return new ZkAttrSnapshot();
		}
		Object obj = XSTREAM.fromXML(new String(data, StandardCharsets.UTF_8));
		ZkAttrSnapshot snap = (ZkAttrSnapshot) obj;
		snap.setLoaded(true);
		return snap;
	}

	/** Ensures full path exists (recursive parent creation) */
	private static void ensurePathExists(ZooKeeper client, String path) throws Exception {
		if (path == null || path.equals("/")) return;
		ZooKeeper zk = client;
		String[] parts = path.substring(1).split("/");
		StringBuilder current = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) continue;
			current.append("/").append(part);
			String currPath = current.toString();
			try {
				Stat stat = zk.exists(currPath, false);
				if (stat == null) {
					zk.create(currPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

				}
			} catch (KeeperException.NodeExistsException ignored) {
				// already exists
			}
		}
	}

	public static void saveAttributes(ZooKeeper client, String rootPath, String document, String xpath,
									  Map<String, Object> values, Map<String, Boolean> setFlags, boolean createPathIfAbsent) throws Exception {
		String znodePath = buildZnodePath(rootPath, document, xpath);

		// Ensure path exists before save
		try {
			ensurePathExists(client, znodePath); // reuse helper if public, or duplicate logic
		} catch (Exception e) {
			// fallback
		}
		ZkAttrSnapshot snap = new ZkAttrSnapshot(values, setFlags);
		byte[] data = XSTREAM.toXML(snap).getBytes(StandardCharsets.UTF_8);

		Stat stat = client.exists(znodePath, false);
		if (stat == null) {
			if (!createPathIfAbsent) {
				throw new ConfigException("ZooKeeper node not found: " + znodePath);
			}
			try {
				client.create(znodePath, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			} catch (KeeperException.NodeExistsException race) {
				client.setData(znodePath, data, -1);
			}
		} else {
			client.setData(znodePath, data, -1);
		}
	}

	private static XStream createXStream() {
		// unchanged
		XStream xs = new XStream(new StaxDriver());
		XStream.setupDefaultSecurity(xs);
		xs.allowTypesByWildcard(new String[] {
				"finalconfigclasses.cfg.zk.**",
				"java.lang.*",
				"java.util.*"
		});
		xs.alias("attr-snapshot", ZkAttrSnapshot.class);
		return xs;
	}
}