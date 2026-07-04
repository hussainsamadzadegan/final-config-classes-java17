package finalconfigclasses.cfg.gen.demo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import finalconfigclasses.cfg.ConfigException;
import finalconfigclasses.cfg.EditResult;
import finalconfigclasses.cfg.EditStrategy;
import finalconfigclasses.cfg.gen.CacheConfigImpl;
import finalconfigclasses.cfg.misc.SimpleDfsEditStrategy;
import finalconfigclasses.cfg.zk.ZkConfigManager;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Demonstrates {@link BusinessClassConfigurator} (via its concrete
 * {@link CacheBusinessClassConfigurator} subclass) keeping a plain,
 * fully-decoupled {@link CacheBusinessComponent} in sync with a
 * {@link CacheConfigImpl}, through the framework's built-in prepare/apply/
 * rollback mechanism. No ZooKeeper/Curator involved.
 *
 * Run with:
 * <pre>
 * mvn -q exec:java -Dexec.mainClass=finalconfigclasses.cfg.gen.CacheBusinessClassConfiguratorDemo
 * </pre>
 */
public final class CacheBusinessClassConfiguratorDemo {

	public static void main(String[] args) throws ConfigException, InterruptedException {
		String connectString = "localhost:2181";

		ZkConfigManager manager = ZkConfigManager.getInstance();
		manager.start(connectString, Integer.MAX_VALUE);


		HashMap<String, Object> defaults = new HashMap<String, Object>();
		defaults.put("cacheSize", 100);
		defaults.put("cachePolicy", "LRU");

		CacheConfigImpl liveConfig = new CacheConfigImpl(
				defaults,
				"/app/config",              // propertiesFile -> ZK root path
				"cache-config-lock",         // lockID
				new ReentrantReadWriteLock(),// propertiesLock
				"prod",                      // document
				null,                        // name
				null                         // keyPrefix
		);
		liveConfig.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("propertyChange: " + evt.getPropertyName() + ", oldValue: " + evt.getOldValue() + ", newValue: " + evt.getNewValue());
			}
		});
		liveConfig.load();

		manager.watch(liveConfig);
		liveConfig.save();
		CacheBusinessComponent cacheComponent = new CacheBusinessComponent();

		System.out.println("--- Wiring up (constructor does the initial sync + subscribes) ---");
		CacheBusinessClassConfigurator configurator =
				new CacheBusinessClassConfigurator(cacheComponent, liveConfig);
		System.out.println("cacheComponent after initial sync: cacheSize=" + cacheComponent.getCacheSize()
				+ ", cachePolicy=" + cacheComponent.getCachePolicy());

		System.out.println("\n--- Scenario 1: valid change ---");
		propose(liveConfig,  Math.random() > 0.5 ? 500:140, "LFU");
		System.out.println("liveConfig     : cacheSize=" + liveConfig.getCacheSize()
				+ ", cachePolicy=" + liveConfig.getCachePolicy());
		System.out.println("cacheComponent : cacheSize=" + cacheComponent.getCacheSize()
				+ ", cachePolicy=" + cacheComponent.getCachePolicy());

		System.out.println("\n--- Scenario 2: invalid change (must be rejected + rolled back) ---");
		propose(liveConfig, -1, "RANDOM");
		System.out.println("liveConfig     unchanged: cacheSize=" + liveConfig.getCacheSize()
				+ ", cachePolicy=" + liveConfig.getCachePolicy());
		System.out.println("cacheComponent unchanged: cacheSize=" + cacheComponent.getCacheSize()
				+ ", cachePolicy=" + cacheComponent.getCachePolicy());
		Thread.sleep(Long.MAX_VALUE);
		configurator.unregister();
	}

	private static void propose(CacheConfigImpl liveConfig, int newCacheSize, String newCachePolicy) {
		CacheConfigImpl proposed = (CacheConfigImpl) liveConfig.clone();
		proposed.setCacheSize(newCacheSize);
		proposed.setCachePolicy(newCachePolicy);

		EditStrategy strategy = new SimpleDfsEditStrategy(liveConfig, proposed);
		strategy.computeDiff();
		strategy.applyUpdate();

		EditResult result = strategy.getEditResult();
		if (result.isSuccessful()) {

			System.out.println("Change accepted and applied.");
		} else if (result.isRejected()) {
			for (finalconfigclasses.bean.BeanUpdateRejectedException reject : result.getRejects()) {
				System.out.println("Rejected: " + reject.getMessage());
			}
		}
	}

	private CacheBusinessClassConfiguratorDemo() {
	}
}
