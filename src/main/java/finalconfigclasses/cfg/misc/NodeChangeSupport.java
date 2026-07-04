package finalconfigclasses.cfg.misc;

import finalconfigclasses.cfg.ConfigBean;

/**
 * A class which broadcast changes to interested listeners.
 */
public final class NodeChangeSupport {

	/**
	 * The source component for lifecycle events that we will fire.
	 */
	private ConfigBean source = null;

	/**
	 * The set of registered LifecycleListeners for event notifications.
	 */
	private NodeChangeListener[] listeners = new NodeChangeListener[0];

	private final Object listenersLock = new Object(); // Lock object for
														// changes to listeners

	/**
	 * Construct a new LifecycleSupport object associated with the specified
	 * Lifecycle component.
	 * 
	 * @param lifecycle
	 *            The Lifecycle component that will be the source of events that
	 *            we fire
	 */
	public NodeChangeSupport(ConfigBean source) {
		if (source == null) {
			throw new NullPointerException();
		}
		this.source = source;
	}

	// --------------------------------------------------------- Public Methods

	/**
	 * Add a lifecycle event listener to this component.
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public void addNodeChangeListener(NodeChangeListener listener) {
		synchronized (listenersLock) {
			NodeChangeListener[] results = new NodeChangeListener[listeners.length + 1];
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
	public NodeChangeListener[] getNodeChangeListeners() {
		return listeners;
	}

	/**
	 * Remove a lifecycle event listener from this component.
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	public void removeNodeChangeListener(NodeChangeListener listener) {
		synchronized (listenersLock) {
			int n = -1;
			for (int i = 0; i < listeners.length; i++) {
				if (listeners[i] == listener) {
					n = i;
					break;
				}
			}
			if (n < 0)
				return;
			NodeChangeListener[] results = new NodeChangeListener[listeners.length - 1];
			int j = 0;
			for (int i = 0; i < listeners.length; i++) {
				if (i != n)
					results[j++] = listeners[i];
			}
			listeners = results;
		}
	}

	public void fireNodeChange(String propertyName, Object oldValue,
			Object newValue) {
		if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
			return;
		}
		fireNodeChange(new NodeChangeEvent(source, propertyName,
				oldValue, newValue));
	}

	public void fireNodeChange(NodeChangeEvent evt) {
		Object oldValue = evt.getOldValue();
		Object newValue = evt.getNewValue();
		String propertyName = evt.getPropertyName();
		if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
			return;
		}
		NodeChangeListener[] interested = listeners;
		for (int i = 0; i < interested.length; i++)
			interested[i].nodeChange(evt);
	}

	public void fireIndexedNodeChange(String propertyName, int index,
			Object oldValue, Object newValue) {
		fireNodeChange(new IndexedNodeChangeEvent(source, propertyName,
				oldValue, newValue, index));
	}

}