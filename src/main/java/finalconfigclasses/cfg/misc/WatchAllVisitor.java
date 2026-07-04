package finalconfigclasses.cfg.misc;

import finalconfigclasses.cfg.ConfigBean;
import finalconfigclasses.cfg.ConfigBeanVisitor;
import finalconfigclasses.cfg.ConfigException;
import finalconfigclasses.cfg.zk.ZkConfigManager;

/**
 * Registers a ZooKeeper watcher for every config bean visited, so that a
 * change to any bean's znode (made by this peer or any other peer in the
 * cluster) triggers that bean to reload automatically.
 *
 * @see ZkConfigManager#watchSubtree(ConfigBean)
 */
public class WatchAllVisitor implements ConfigBeanVisitor {

	private final ZkConfigManager manager;

	public WatchAllVisitor(ZkConfigManager manager) {
		this.manager = manager;
	}

	public boolean terminate() {
		return false;
	}

	public void visitAfterChildren(ConfigBean bean) {
	}

	public void visitBeforeChildren(ConfigBean bean) throws ConfigException {
		manager.watch(bean);
	}

}
