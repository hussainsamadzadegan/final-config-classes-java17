package finalconfigclasses.cfg.misc;

import finalconfigclasses.cfg.ConfigBean;

@SuppressWarnings("serial")
public class IndexedNodeChangeEvent extends NodeChangeEvent {

	private int index;

	public IndexedNodeChangeEvent(ConfigBean source, String propertyName,
			Object oldValue, Object newValue, int index) {
		super(source, propertyName, oldValue, newValue);
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
