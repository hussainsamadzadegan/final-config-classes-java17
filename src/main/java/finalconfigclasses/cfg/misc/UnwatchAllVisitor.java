package finalconfigclasses.cfg.misc;

import finalconfigclasses.cfg.ConfigBean;
import finalconfigclasses.cfg.ConfigBeanVisitor;
import finalconfigclasses.cfg.zk.ZkConfigManager;

/**
 * Cancels the ZooKeeper watcher for every config bean visited.
 *
 * @see ZkConfigManager#unwatchSubtree(ConfigBean)
 */
public class UnwatchAllVisitor implements ConfigBeanVisitor {

	private final ZkConfigManager manager;

	public UnwatchAllVisitor(ZkConfigManager manager) {
		this.manager = manager;
	}

	public boolean terminate() {
		return false;
	}

	public void visitAfterChildren(ConfigBean bean) {
	}

	public void visitBeforeChildren(ConfigBean bean) {
		manager.unwatch(bean);
	}

}
