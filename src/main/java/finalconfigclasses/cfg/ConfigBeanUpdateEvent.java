package finalconfigclasses.cfg;

import finalconfigclasses.bean.BeanUpdateEvent;

public abstract class ConfigBeanUpdateEvent extends BeanUpdateEvent {

	protected ConfigBeanUpdateEvent(ConfigBean sourceBean,
			ConfigBean proposedBean/*, int updateID*/) {
		super(sourceBean, proposedBean/*, updateID*/);
	}

	public ConfigBean getSourceBean() {
		return (ConfigBean) super.getSourceBean();
	}

	public ConfigBean getProposedBean() {
		return (ConfigBean) super.getProposedBean();
	}
}
