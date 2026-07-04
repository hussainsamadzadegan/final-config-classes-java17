package finalconfigclasses.bean;

import java.util.EventListener;

/**
 * This listener provides a mechanisem to reject updates on beans(e.g. config beans) so
 * that all updates(which are in cloned version) not be applied on source bean. I think
 * it is better to use this class instead of VetoableChangeListener from JavaBeans.
 * 
 * DiffHelper classes first notify all BeanUpdateListener(s) which are registered for
 * changes(DiffHeplper notifies the prepareUpdate method on listeners), and if all
 * BeanUpdateListener(s) were agree for changes then DiffHelper would write changes
 * on source bean(and also would call activeUpdate() method on BeanUpdateListeners and
 * call propertyChange() method on PropertyChangeListeners).
 * 
 * But if one of the BeanUpdateListeners were not agree with changes then DiffHelper
 * would not write changes on source bean(it ignores the update) and also would call
 * the rollbackUpdate() method of BeanUpdateListeners.
 * 
 * It it important to know that DiffHelper would call all BeanUpdateListeners which
 * are registered on all beans on bean Hierarchy.
 * 
 * Using BeanUpdateListener (instead of PropertyChangeListener) you can have a chance
 * to inspect all updates and then reject them if you are not agree with changes in 
 * this moment.
 * 
 * BeanUpdateListeners are complement to PropertyChangeListeners that you can use both.
 * But if you need to inspect the changes you can use BeanUpdateListener.
 * 
 * To show the use case, you may want to reject updates if the number of concurrent users
 * are so high(the system is under high traffic), and you want to notify the admin(on UI) that
 * changing the number of jdbc connections are risky, so you may register a BeanUpdateListener
 * on jdbc-config bean and when the admin updates this config you can reject the config(by throwing
 * the BeanUpdateRejectedException inside your listener). Note that if you are agree with
 * changes you should update consumer classes inside activateUpdate method.
 * 
 * Here is a typical form of one BeanUpdateListener:
 * 
 * 		public class MyListener implements BeanUpdateListener {
 * 			private final SomeClass customerObject;
 * 			
 * 			public MyListener(final SomeClass customerObject) {
 * 				this.customerObject = customerObject;
 * 			}
 * 
 * 			public void prepareUpdate(BeanUpdateEvent event)
 *				throws BeanUpdateRejectedException {
 *				
 *				//the proposed bean is the config object which contains updates and new values
 *				ConfigBean cfgBean = (ConfigBean)event.getProposedBean();
 *
 *				if(cfgBean instanceof CacheConfig) {
 *					CacheConfig cc = (CacheConfig) cfgBean;
 *					if(cc.getCacheSize() > 100000) {
 *						//this size of cache is not acceptable for current moment.
 *						//maybe after 30 minutes(when the traffic is low) this value
 *						//be accaptable.
 *
 *						throw new BeanUpdateRejectedException("The size of cache is very high!");
 *					}
 *				}
 *			}
 *			
 *  		public void activateUpdate(BeanUpdateEvent event)
 *				throws BeanUpdateFailedException {
 *				
 *				//the proposed bean is the config object which contains updates and new values
 *				ConfigBean cfgBean = (ConfigBean)event.getProposedBean();
 *
 *				if(cfgBean instanceof CacheConfig) {
 *					CacheConfig cc = (CacheConfig) cfgBean;
 *					customerObject.setCacheSize(cc.getCacheSize());
 *				}				
 *			}
 *			
 *			public void rollbackUpdate(BeanUpdateEvent event) {
 *				//nothing to do in this example.
 *			}
 *	
 *		}
 *			
 */
public interface BeanUpdateListener extends EventListener {
	
	public void prepareUpdate(BeanUpdateEvent event)
			throws BeanUpdateRejectedException;

	public void activateUpdate(BeanUpdateEvent event)
			throws BeanUpdateFailedException;

	public void rollbackUpdate(BeanUpdateEvent event);
	
}
