package finalconfigclasses.cfg;

import java.util.Map;

/**
 * This class encapsulates algorithm to apply changes.
 */
public abstract class EditStrategy {

	protected final ConfigBean sourceBean;
	protected final ConfigBean proposedBean;

	public EditStrategy(ConfigBean sourceBean, ConfigBean proposedBean) {
		if (sourceBean == null)
			throw new IllegalArgumentException("null source");
		if (proposedBean == null)
			throw new IllegalArgumentException("null proposed");
		this.sourceBean = sourceBean;
		this.proposedBean = proposedBean;
	}

	public abstract void computeDiff();

	public abstract boolean hasNonDynamicUpdates();

	public abstract void applyUpdate();

	public abstract EditResult getEditResult();

	public abstract Map<String, Object> getParameterMap();

}
