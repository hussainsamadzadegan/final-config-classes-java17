// src/main/java/finalconfigclasses/cfg/zk/ZkConfigManager.java
package finalconfigclasses.cfg.zk;

import finalconfigclasses.cfg.ConfigBean;
import finalconfigclasses.cfg.ConfigException;
import finalconfigclasses.cfg.misc.UnwatchAllVisitor;
import finalconfigclasses.cfg.misc.WatchAllVisitor;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ZkConfigManager {

	private static final Logger LOG = Logger.getLogger(ZkConfigManager.class.getName());

	private static final class SingletonHolder {
		static final ZkConfigManager THE_ONE = new ZkConfigManager();
	}

	public static ZkConfigManager getInstance() {
		return SingletonHolder.THE_ONE;
	}

	private volatile ZooKeeper client;
	private final Object startLock = new Object();

	private final Map<ConfigBean, ZkBeanWatcher> watches = new IdentityHashMap<>();
	private final Object watchLock = new Object();

	private final Set<ConfigBean> pendingReloads = java.util.Collections.newSetFromMap(new IdentityHashMap<>());

	private final ExecutorService reloadExecutor = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "zk-config-reload");
		t.setDaemon(true);
		return t;
	});

	private ZkConfigManager() {}

	public void start(String connectString) {
		start(connectString, 30000);
	}

	public void start(String connectString, int sessionTimeout) {
		if (client != null) return;
		synchronized (startLock) {
			if (client != null) return;
			try {
				client = new ZooKeeper(connectString, sessionTimeout, event -> {});
				// Wait for connection
				long deadline = System.currentTimeMillis() + 15000;
				while (client.getState() != ZooKeeper.States.CONNECTED && System.currentTimeMillis() < deadline) {
					Thread.sleep(200);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to connect to ZooKeeper", e);
			}
		}
	}

	public void useClient(ZooKeeper zk) {
		this.client = zk;
	}

	public ZooKeeper getClient() {
		if (client == null) throw new IllegalStateException("ZkConfigManager not started");
		return client;
	}

	/** Ensures full path exists (recursive parent creation) */
	private void ensurePathExists(String path) throws Exception {
		if (path == null || path.equals("/")) return;
		ZooKeeper zk = getClient();
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
					LOG.info("Created znode: " + currPath);
				}
			} catch (KeeperException.NodeExistsException ignored) {
				// already exists
			}
		}
	}

	public void watch(ConfigBean bean) throws ConfigException {
		if (bean == null) throw new IllegalArgumentException("bean is null");
		final String path = ZkConfigStore.buildZnodePath(
				bean._getPropertiesFile(), bean._getDocument(), bean._getXPath());

		synchronized (watchLock) {
			if (watches.containsKey(bean)) return;

			ZkBeanWatcher watcher = new ZkBeanWatcher(bean, path);
			watches.put(bean, watcher);

			try {
				ensurePathExists(path);
				getClient().exists(path, watcher); // register watcher
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Failed to watch " + path, e);
			}
		}
	}

	// watchSubtree, unwatch, etc. unchanged...

	public void watchSubtree(ConfigBean root) throws ConfigException {
		try {
			root.accept(new WatchAllVisitor(this));
		} catch (Exception e) {
			throw new ConfigException(e);
		}
	}

	public void unwatch(ConfigBean bean) {
		if (bean == null) return;
		ZkBeanWatcher w;
		synchronized (watchLock) {
			w = watches.remove(bean);
		}
		if (w != null) w.close();
	}

	public void unwatchSubtree(ConfigBean root) throws ConfigException {
		try {
			root.accept(new UnwatchAllVisitor(this));
		} catch (Exception e) {
			throw new ConfigException(e);
		}
	}

	void scheduleReload(ConfigBean bean, String path) {
		synchronized (pendingReloads) {
			if (!pendingReloads.add(bean)) return;
		}
		reloadExecutor.submit(() -> {
			try {
				bean.load();
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Reload failed for " + path, e);
			} finally {
				synchronized (pendingReloads) {
					pendingReloads.remove(bean);
				}
			}
		});
	}

	public void shutdown() {
		synchronized (watchLock) {
			watches.values().forEach(ZkBeanWatcher::close);
			watches.clear();
		}
		reloadExecutor.shutdown();
		ZooKeeper c = client;
		if (c != null) {
			try { c.close(); } catch (Exception ignored) {}
			client = null;
		}
	}

	private class ZkBeanWatcher implements Watcher {
		private final ConfigBean bean;
		private final String path;
		private volatile boolean closed = false;

		ZkBeanWatcher(ConfigBean bean, String path) {
			this.bean = bean;
			this.path = path;
		}

		@Override
		public void process(WatchedEvent event) {
			if (closed || !path.equals(event.getPath())) return;
			if (event.getType() == Watcher.Event.EventType.NodeDataChanged ||
					event.getType() == Watcher.Event.EventType.NodeCreated) {
				scheduleReload(bean, path);
			}
			// Re-register
			if (!closed) {
				try {
					getClient().exists(path, this);
				} catch (Exception ignored) {}
			}
		}

		void close() {
			closed = true;
		}
	}
}