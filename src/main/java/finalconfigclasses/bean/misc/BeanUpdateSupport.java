package finalconfigclasses.bean.misc;

import finalconfigclasses.bean.BeanUpdateEvent;
import finalconfigclasses.bean.BeanUpdateFailedException;
import finalconfigclasses.bean.BeanUpdateListener;
import finalconfigclasses.bean.BeanUpdateRejectedException;

/**
 * A class which broadcast changes to interested listeners.
 */
public final class BeanUpdateSupport
{

	/**
	 * The source component for lifecycle events that we will fire.
	 */
	private Object source = null;
	
	
	/**
	 * The set of registered LifecycleListeners for event notifications.
	 */
	private BeanUpdateListener[] listeners = new BeanUpdateListener[0];
	
	private final Object listenersLock = new Object(); // Lock object for changes to listeners
	
	/**
	 * Construct a new LifecycleSupport object associated with the specified
	 * Lifecycle component.
	 *
	 * @param lifecycle The Lifecycle component that will be the source
	 *  of events that we fire
	 */
	public BeanUpdateSupport(Object source)
	{
	    if (source == null) {
	    throw new NullPointerException();
	    }
	    this.source = source;
	}
	
	// --------------------------------------------------------- Public Methods
	
	
	/**
	 * Add a lifecycle event listener to this component.
	 *
	 * @param listener The listener to add
	 */
	public void addBeanUpdateListener(BeanUpdateListener listener)
	{
	  synchronized(listenersLock) {
	      BeanUpdateListener[] results =
	        new BeanUpdateListener[listeners.length + 1];
	      for (int i = 0; i < listeners.length; i++)
	          results[i] = listeners[i];
	      results[listeners.length] = listener;
	      listeners = results;
	  }	
	}
	
	/**
	 * Get the lifecycle listeners associated with this lifecycle. If this 
	 * Lifecycle has no listeners registered, a zero-length array is returned.
	 */
	public BeanUpdateListener[] getBeanUpdateListeners()
	{
	    return listeners;
	}
	
	/**
	 * Remove a lifecycle event listener from this component.
	 *
	 * @param listener The listener to remove
	 */
	public void removeBeanUpdateListener(BeanUpdateListener listener)
	{
	    synchronized(listenersLock) {
	        int n = -1;
	        for (int i = 0; i < listeners.length; i++) {
	            if (listeners[i] == listener) {
	                n = i;
	                break;
	            }
	        }
	        if (n < 0)
	            return;
	        BeanUpdateListener[] results =
	          new BeanUpdateListener[listeners.length - 1];
	        int j = 0;
	        for (int i = 0; i < listeners.length; i++) {
	            if (i != n)
	                results[j++] = listeners[i];
	        }
	        listeners = results;
	    }
	}
	
	public void firePrepareUpdate(BeanUpdateEvent evt) throws BeanUpdateRejectedException {
	    BeanUpdateListener[] interested = listeners;
	    for (int i = 0; i < interested.length; i++)
	        interested[i].prepareUpdate(evt);
	}
	
	public void fireActivateUpdate(BeanUpdateEvent evt) throws BeanUpdateFailedException {
	    BeanUpdateListener[] interested = listeners;
	    for (int i = 0; i < interested.length; i++)
	        interested[i].activateUpdate(evt);
	}
	
	public void fireRollbackUpdate(BeanUpdateEvent evt) {
	    BeanUpdateListener[] interested = listeners;
	    for (int i = 0; i < interested.length; i++)
	        interested[i].rollbackUpdate(evt);
	}

}