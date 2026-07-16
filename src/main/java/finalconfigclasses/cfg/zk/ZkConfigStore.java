package finalconfigclasses.cfg.zk;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import finalconfigclasses.cfg.ConfigException;

/**
 * Persists/loads using CuratorFramework (Curator handles session-expiry
 * recovery and retries per its RetryPolicy - the raw ZooKeeper client this
 * used to take did neither).
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

	public static ZkAttrSnapshot loadAttributes(CuratorFramework client, String rootPath, String document,
												String xpath, boolean createPathIfAbsent) throws Exception {
		String znodePath = buildZnodePath(rootPath, document, xpath);
		byte[] data;
		try {
			data = client.getData().forPath(znodePath);
		} catch (KeeperException.NoNodeException nne) {
			if (!createPathIfAbsent) {
				throw new ConfigException("ZooKeeper node not found: " + znodePath, nne);
			}
			try {
				client.create().creatingParentsIfNeeded().forPath(znodePath, new byte[0]);
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
	private static void ensurePathExists(CuratorFramework client, String path) throws Exception {
		if (path == null || path.equals("/")) return;
		Stat stat = client.checkExists().forPath(path);
		if (stat == null) {
			try {
				client.create().creatingParentsIfNeeded().forPath(path, new byte[0]);
			} catch (KeeperException.NodeExistsException ignored) {
				// already exists (race)
			}
		}
	}

	public static void saveAttributes(CuratorFramework client, String rootPath, String document, String xpath,
									  Map<String, Object> values, Map<String, Boolean> setFlags, boolean createPathIfAbsent) throws Exception {
		String znodePath = buildZnodePath(rootPath, document, xpath);

		// Ensure path exists before save
		try {
			ensurePathExists(client, znodePath);
		} catch (Exception e) {
			// fallback
		}
		ZkAttrSnapshot snap = new ZkAttrSnapshot(values, setFlags);
		byte[] data = XSTREAM.toXML(snap).getBytes(StandardCharsets.UTF_8);

		Stat stat = client.checkExists().forPath(znodePath);
		if (stat == null) {
			if (!createPathIfAbsent) {
				throw new ConfigException("ZooKeeper node not found: " + znodePath);
			}
			try {
				client.create().creatingParentsIfNeeded().forPath(znodePath, data);
			} catch (KeeperException.NodeExistsException race) {
				client.setData().forPath(znodePath, data);
			}
		} else {
			client.setData().forPath(znodePath, data);
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