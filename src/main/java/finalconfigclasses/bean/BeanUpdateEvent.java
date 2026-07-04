package finalconfigclasses.bean;

import java.util.EventObject;
import java.util.List;

public abstract class BeanUpdateEvent extends EventObject {
	
	private final Object proposedBean;
	/*private final int updateID;*/
	
	/**
	 * @param sourceBean the source bean which is the base of difference operation
	 * @param proposedBean the destination bean would be compared against source bean
	 * 
	 * The changes between source and destination finally would be written on
	 * source bean.
	 */
	protected BeanUpdateEvent(Object sourceBean,
			Object proposedBean/*, int updateID*/) {
		super(sourceBean);
		this.proposedBean = proposedBean;
		/*this.updateID = updateID;*/
	}

	/*public int getUpdateID() {
		return updateID;
	}*/

	public Object getSource() {
		return super.getSource();
	}

	public Object getSourceBean() {
		return getSource();
	}

	public Object getProposedBean() {
		return proposedBean;
	}

	public abstract List<PropertyUpdate> getUpdateList();

}
