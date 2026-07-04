package finalconfigclasses.cfg.misc;

import finalconfigclasses.cfg.ConfigBean;
import finalconfigclasses.cfg.ConfigBeanVisitor;
import finalconfigclasses.cfg.ConfigException;

public class LoadAllVisitor implements ConfigBeanVisitor {

	public boolean terminate() {
		return false;
	}

	public void visitAfterChildren(ConfigBean bean) {
	}

	public void visitBeforeChildren(ConfigBean bean) throws ConfigException {
		bean.load();
	}

}
