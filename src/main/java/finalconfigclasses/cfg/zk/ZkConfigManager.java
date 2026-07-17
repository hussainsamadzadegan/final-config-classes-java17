// src/main/java/finalconfigclasses/cfg/zk/ZkConfigManager.java
package finalconfigclasses.cfg.zk;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import finalconfigclasses.cfg.ConfigBean;
import finalconfigclasses.cfg.ConfigDiffHelper;
import finalconfigclasses.cfg.ConfigException;
import finalconfigclasses.cfg.Registry;
import finalconfigclasses.cfg.gen.BankConfigImpl;
import finalconfigclasses.cfg.misc.UnwatchAllVisitor;
import finalconfigclasses.cfg.misc.WatchAllVisitor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

	private volatile CuratorFramework client;
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
				CuratorFramework curatorClient = CuratorFrameworkFactory.builder()
						.connectString(connectString)
						.sessionTimeoutMs(sessionTimeout)
						.connectionTimeoutMs(15000)
						.retryPolicy(new ExponentialBackoffRetry(1000, 5))
						.build();

				curatorClient.getConnectionStateListenable().addListener(this::onConnectionStateChanged);
				curatorClient.start();
				curatorClient.blockUntilConnected(15, TimeUnit.SECONDS);
				client = curatorClient;
			} catch (Exception e) {
				throw new RuntimeException("Failed to connect to ZooKeeper", e);
			}
		}
	}

	private void onConnectionStateChanged(CuratorFramework c, ConnectionState newState) {
		switch (newState) {
			case LOST:
				LOG.warning("ZooKeeper session lost - Curator will establish a new session on reconnect");
				break;
			case RECONNECTED:
				LOG.warning("ZooKeeper reconnected with a NEW session - active NodeCache watches resync automatically");
				break;
			case SUSPENDED:
				LOG.warning("ZooKeeper connection suspended - operations will retry per RetryPolicy");
				break;
			default:
				break;
		}
	}

	/** Kept for tests/manual wiring; now takes a CuratorFramework instead of a raw ZooKeeper handle. */
	public void useClient(CuratorFramework curatorClient) {
		this.client = curatorClient;
	}

	public CuratorFramework getClient() {
		if (client == null) throw new IllegalStateException("ZkConfigManager not started");
		return client;
	}

	/** Ensures full path exists (recursive parent creation) */
	private void ensurePathExists(String path) throws Exception {
		if (path == null || path.equals("/")) return;
		CuratorFramework zk = getClient();
		Stat stat = zk.checkExists().forPath(path);
		if (stat == null) {
			try {
				zk.create().creatingParentsIfNeeded().forPath(path, new byte[0]);
				LOG.info("Created znode: " + path);
			} catch (KeeperException.NodeExistsException ignored) {
				// already exists (race)
			}
		}
	}

	public void watch(ConfigBean bean) throws ConfigException {
		if (bean == null) throw new IllegalArgumentException("bean is null");
		final String path = ZkConfigStore.buildZnodePath(
				bean._getPropertiesFile(), bean._getDocument(), bean._getXPath());

		synchronized (watchLock) {
			if (watches.containsKey(bean)) return;

			try {
				ensurePathExists(path);

				NodeCache nodeCache = new NodeCache(getClient(), path);
				ZkBeanWatcher watcher = new ZkBeanWatcher(bean, path, nodeCache);
				nodeCache.getListenable().addListener(watcher::onNodeChanged);
				nodeCache.start();

				watches.put(bean, watcher);
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Failed to watch " + path, e);
			}
		}
	}

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
				System.out.println("[Zk] Change detected on znode: " + path
						+ " | Node: " + System.getProperty("node.id", "unknown"));

				if (bean instanceof BankConfigImpl liveBean) {
					// 1. Clone the CURRENT state BEFORE loading new data
					BankConfigImpl originalState = (BankConfigImpl) liveBean.clone();

					// 2. Load latest data from ZooKeeper
					liveBean.load();

					// 3. Now compute diff between old state and new loaded state
					ConfigDiffHelper diffHelper = originalState._newDiffHelper();
					diffHelper.computeDiff(liveBean);   // Note: source = old, target = new (live)

					if (diffHelper.getBeanDiff() != null && diffHelper.getBeanDiff().size() > 0) {
						System.out.println("[Zk] UpdateSet size: " + diffHelper.getBeanDiff().size());
						System.out.println(diffHelper.getBeanDiff());
					} else {
						System.out.println("[Zk] No changes detected after load");
					}
					// 4. Trigger BeanUpdate flow
					diffHelper.applyUpdate();

					System.out.println("[Zk] BeanUpdateEvent triggered on remote node");
				} else {
					bean.load();
				}

			} catch (Exception e) {
				LOG.log(Level.WARNING, "Failed to process update for " + path, e);
				try {
					bean.load();
				} catch (Exception ex) {
					LOG.log(Level.SEVERE, "Fallback failed", ex);
				}
			} finally {
				synchronized (pendingReloads) {
					pendingReloads.remove(bean);
				}
			}
		});
	}

	private XStream createXStreamForFullBean() {
		XStream xs = new XStream(new StaxDriver());
		XStream.setupDefaultSecurity(xs);
		xs.allowTypesByWildcard(new String[] {
				"finalconfigclasses.cfg.**",
				"finalconfigclasses.cfg.gen.**",
				"java.lang.*",
				"java.util.*"
		});
		return xs;
	}
	private void applySnapshotToBean(BankConfigImpl bean, ZkAttrSnapshot snap) {
		try {
			// You can expand this to update all attributes from the snapshot
			// For now, let's reload the whole bean (safest)
			bean.load();

			System.out.println("Applied snapshot and reloaded BankConfigImpl");
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Failed to apply snapshot to bean", e);
		}
	}

	public void shutdown() {
		synchronized (watchLock) {
			watches.values().forEach(ZkBeanWatcher::close);
			watches.clear();
		}
		reloadExecutor.shutdown();
		CuratorFramework c = client;
		if (c != null) {
			try { c.close(); } catch (Exception ignored) {}
			client = null;
		}
	}

	/**
	 * Wraps a Curator NodeCache instead of a raw one-shot Watcher.
	 * NodeCache re-establishes its own watch after every reconnect/session
	 * replacement, so - unlike the old raw-Watcher version - this survives
	 * SessionExpiredException without any manual re-registration.
	 */
	private class ZkBeanWatcher {
		private final ConfigBean bean;
		private final String path;
		private final NodeCache nodeCache;
		private volatile boolean closed = false;
		private volatile boolean first = true;

		ZkBeanWatcher(ConfigBean bean, String path, NodeCache nodeCache) {
			this.bean = bean;
			this.path = path;
			this.nodeCache = nodeCache;
		}

		void onNodeChanged() {
			if (closed) return;
			// NodeCache fires once immediately on start() with the current
			// data; skip that initial callback so we don't reload right
			// after the bean was just loaded/watched.
			if (first) {
				first = false;
				return;
			}
			scheduleReload(bean, path);
		}

		void close() {
			closed = true;
			try {
				nodeCache.close();
			} catch (Exception ignored) {}
		}
	}
}