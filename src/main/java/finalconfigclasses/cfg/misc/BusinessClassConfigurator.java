package finalconfigclasses.cfg.misc;

import finalconfigclasses.bean.BeanUpdateEvent;
import finalconfigclasses.bean.BeanUpdateFailedException;
import finalconfigclasses.bean.BeanUpdateListener;
import finalconfigclasses.bean.BeanUpdateRejectedException;
import finalconfigclasses.cfg.ConfigBean;

/**
 * Bridges a plain "business" object (which knows nothing about
 * {@code finalconfigclasses.cfg.*}) to a config bean, keeping the two
 * decoupled: the business class declares its own fields/setters (e.g.
 * {@code cacheSize}, {@code cachePolicy}) independently of - and duplicated
 * from - the matching config bean's attributes. Nothing about the business
 * class's API reveals that it happens to be configured from a
 * {@code ConfigBean} at all; it could just as easily be constructed with
 * literal values in a unit test.
 *
 * On construction:
 * <ol>
 *   <li>the business object is immediately initialized from the config
 *       bean's <em>current</em> values, via the business class's own
 *       setters ({@link #copyToBusinessObject});</li>
 *   <li>this configurator then registers itself as a {@link BeanUpdateListener}
 *       on the config bean, so it also handles every future change.</li>
 * </ol>
 *
 * From then on, every proposed config change to {@code configBean} runs
 * through the usual prepare/apply/rollback sequence (see
 * {@link SimpleDfsEditStrategy}):
 * <ul>
 *   <li><b>prepare</b> - {@link #validate(ConfigBean)} is called with the
 *       <em>proposed</em> config bean; throw {@link BeanUpdateRejectedException}
 *       to veto the change. The business object is not touched yet.</li>
 *   <li><b>apply</b> - only once every registered listener has agreed, the
 *       config bean's own attributes are updated, and then
 *       {@link #copyToBusinessObject} pushes the new values onto the
 *       business object via its setters.</li>
 *   <li><b>rollback</b> - if any listener rejected, neither the config bean
 *       nor the business object are touched at all;
 *       {@link #onRollback(BeanUpdateEvent)} is called purely as an
 *       (optional) notification hook.</li>
 * </ul>
 *
 * @param <B> the business class type (no dependency on this framework)
 * @param <C> the config bean type this business class is configured from
 */
public abstract class BusinessClassConfigurator<B, C extends ConfigBean> implements BeanUpdateListener {

	protected final B businessObject;
	protected final C configBean;

	protected BusinessClassConfigurator(B businessObject, C configBean) {
		if (businessObject == null) {
			throw new IllegalArgumentException("businessObject is null");
		}
		if (configBean == null) {
			throw new IllegalArgumentException("configBean is null");
		}
		this.businessObject = businessObject;
		this.configBean = configBean;

		// Step 1: initial sync, straight from the config bean's current
		// values, before this configurator is subscribed to anything - so
		// the business object always starts in a valid, fully-initialized
		// state.
		copyToBusinessObject(configBean, businessObject);

		// Step 2: from now on, also react to every future proposed change.
		configBean.addBeanUpdateListener(this);
	}

	// ---- BeanUpdateListener -------------------------------------------------

	@SuppressWarnings("unchecked")
	public final void prepareUpdate(BeanUpdateEvent event) throws BeanUpdateRejectedException {
		Object proposed = event.getProposedBean();
		if (!getConfigBeanClass().isInstance(proposed)) {
			return; // this event isn't about our config bean type - ignore it
		}
		validate((C) proposed);
	}

	@SuppressWarnings("unchecked")
	public final void activateUpdate(BeanUpdateEvent event) throws BeanUpdateFailedException {
		Object sourceAfterUpdate = event.getSourceBean();
		if (!getConfigBeanClass().isInstance(sourceAfterUpdate)) {
			return;
		}
		copyToBusinessObject((C) sourceAfterUpdate, businessObject);
	}

	public final void rollbackUpdate(BeanUpdateEvent event) {
		// Nothing to undo: the config bean's attributes were never written
		// (the whole proposal is discarded on any reject - see
		// SimpleDfsEditStrategy) and activateUpdate() never ran, so
		// businessObject is exactly as it was before this proposal.
		onRollback(event);
	}

	/** Stops reacting to future config changes. */
	public final void unregister() {
		configBean.removeBeanUpdateListener(this);
	}

	// ---- hooks for subclasses -------------------------------------------

	/**
	 * Validates a <em>proposed</em> config bean against whatever this
	 * business object requires to keep working correctly. Throw
	 * {@link BeanUpdateRejectedException} to veto the whole transaction
	 * (nothing will be applied, to this business object or the config bean).
	 */
	protected abstract void validate(C proposedConfig) throws BeanUpdateRejectedException;

	/**
	 * Pushes a config bean's current values onto the business object,
	 * exclusively via the business object's own setters - this is the one
	 * place that knows about both types; the business class itself never
	 * needs to.
	 */
	protected abstract void copyToBusinessObject(C configSource, B businessObject);

	/** Returns the config bean type this configurator handles (for the instanceof checks above). */
	protected abstract Class<C> getConfigBeanClass();

	/** Optional hook for subclasses that want to react to a rollback (e.g. logging/metrics/alerting). */
	protected void onRollback(BeanUpdateEvent event) {
	}
}
