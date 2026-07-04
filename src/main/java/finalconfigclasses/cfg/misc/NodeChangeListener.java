package finalconfigclasses.cfg.misc;

import java.util.EventListener;

public interface NodeChangeListener extends EventListener {
	
	public void nodeChange(NodeChangeEvent evt);
	
}
