package finalconfigclasses.cfg;

import finalconfigclasses.bean.PropertyUpdate;

public class ConfigPropertyUpdate extends PropertyUpdate {
	
	private boolean isDynamic;
	private boolean originalSetBit;
	private boolean proposedSetBit;
	
	public ConfigPropertyUpdate(String propertyName, boolean isDynamic, boolean originalSetBit,
			boolean proposedSetBit) {
		super(propertyName);
		this.isDynamic = isDynamic;
		this.originalSetBit = originalSetBit;
		this.proposedSetBit = proposedSetBit;
	}

	public ConfigPropertyUpdate(String propertyName, UpdateType updateType, Object addedOrRemoved, boolean isDynamic,
			boolean originalSetBit, boolean proposedSetBit) {
		super(propertyName, updateType, addedOrRemoved);
		this.isDynamic = isDynamic;
		this.originalSetBit = originalSetBit;
		this.proposedSetBit = proposedSetBit;
	}
	
	public String toString() {
		switch (updateType) {
		case CHANGE:
			return (new StringBuilder()).append(propertyName).append(
					" (CHANGE)(Dynamic=").append(isDynamic()).append(")")
					.toString();

		case ADD:
			return (new StringBuilder()).append(propertyName).append(
					" (ADD ").append(addedOrRemoved).append(")(Dynamic=")
					.append(isDynamic()).append(")").toString();

		case REMOVE:
			return (new StringBuilder()).append(propertyName).append(
					" (REMOVE ").append(addedOrRemoved)
					.append(")(Dynamic=").append(isDynamic()).append(")")
					.toString();
		}
		throw new AssertionError((new StringBuilder()).append(
				"Change type ").append(updateType).append(" illegal")
				.toString());
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof ConfigPropertyUpdate))
			return false;
		ConfigPropertyUpdate propertyupdate = (ConfigPropertyUpdate) obj;
		if (!propertyName.equals(propertyupdate.propertyName))
			return false;
		if (updateType != propertyupdate.updateType)
			return false;
		else
			return addedOrRemoved == propertyupdate.addedOrRemoved;
	}

	public boolean isDynamic() {
		return isDynamic;
	}

	public boolean isDerivedUpdate() {
		return !originalSetBit && !proposedSetBit;
	}

	public boolean isUnsetUpdate() {
		return originalSetBit && !proposedSetBit;
	}
}