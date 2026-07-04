package finalconfigclasses.bean;

public class PropertyUpdate {

	public static enum UpdateType {
		CHANGE,
		ADD,
		REMOVE
	}

	protected String propertyName;
	protected UpdateType updateType;
	protected Object addedOrRemoved;
	
	public PropertyUpdate(String propertyName) {
		this.propertyName = propertyName;
		updateType = UpdateType.CHANGE;
	}

	public PropertyUpdate(String propertyName, UpdateType updateType, Object addedOrRemoved) {
		this.propertyName = propertyName;
		this.updateType = updateType;
		this.addedOrRemoved = addedOrRemoved;
	}
	
	public String getPropertyName() {
		return propertyName;
	}

	public UpdateType getUpdateType() {
		return updateType;
	}

	public Object getAddedObject() {
		return updateType != UpdateType.ADD ? null : addedOrRemoved;
	}

	public void resetAddedObject(Object obj) {
		addedOrRemoved = obj;
	}

	public Object getRemovedObject() {
		return updateType != UpdateType.REMOVE ? null : addedOrRemoved;
	}

	public String toString() {
		switch (updateType) {
		case CHANGE: 
			return (new StringBuilder()).append(propertyName).append(
					" (CHANGE)").toString();

		case ADD: 
			return (new StringBuilder()).append(propertyName).append(
					" (ADD ").append(addedOrRemoved).append(")")
					.toString();

		case REMOVE: 
			return (new StringBuilder()).append(propertyName).append(
					" (REMOVE ").append(addedOrRemoved)
					.append(")").toString();
		}
		throw new AssertionError((new StringBuilder()).append(
				"Change type ").append(updateType).append(" illegal")
				.toString());
	}

	public int hashCode() {
		return propertyName.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof PropertyUpdate))
			return false;
		PropertyUpdate propertyupdate = (PropertyUpdate) obj;
		if (!propertyName.equals(propertyupdate.propertyName))
			return false;
		if (updateType != propertyupdate.updateType)
			return false;
		else
			return addedOrRemoved == propertyupdate.addedOrRemoved;
	}

	public boolean isRemoveUpdate() {
		return updateType == UpdateType.REMOVE;
	}

	public boolean isChangeUpdate() {
		return updateType == UpdateType.CHANGE;
	}
}