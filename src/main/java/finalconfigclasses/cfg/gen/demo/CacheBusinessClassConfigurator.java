package finalconfigclasses.cfg.gen.demo;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import finalconfigclasses.bean.BeanUpdateRejectedException;
import finalconfigclasses.cfg.gen.CacheConfigImpl;
import finalconfigclasses.cfg.misc.BusinessClassConfigurator;

/**
 * The one place in the app that knows about both {@link CacheBusinessComponent}
 * (business/runtime side) and {@link CacheConfigImpl} (config side). Its
 * whole job is copying values across via each side's own API - it never
 * exposes either type's internals to the other.
 */
public final class CacheBusinessClassConfigurator
		extends BusinessClassConfigurator<CacheBusinessComponent, CacheConfigImpl> {

	private static final Set<String> ALLOWED_POLICIES =
			new LinkedHashSet<String>(Arrays.asList("LRU", "LFU", "FIFO"));

	public CacheBusinessClassConfigurator(CacheBusinessComponent businessObject, CacheConfigImpl configBean) {
		super(businessObject, configBean);
	}

	@Override
	protected void validate(CacheConfigImpl proposedConfig) throws BeanUpdateRejectedException {
		if (proposedConfig.getCacheSize() <= 0 || proposedConfig.getCacheSize() > 100000) {
			throw new BeanUpdateRejectedException(
					"cacheSize must be between 1 and 100000, got " + proposedConfig.getCacheSize());
		}
		if (proposedConfig.getCachePolicy() == null || !ALLOWED_POLICIES.contains(proposedConfig.getCachePolicy())) {
			throw new BeanUpdateRejectedException(
					"cachePolicy must be one of " + ALLOWED_POLICIES + ", got " + proposedConfig.getCachePolicy());
		}
	}

	@Override
	protected void copyToBusinessObject(CacheConfigImpl configSource, CacheBusinessComponent businessObject) {
		businessObject.setCacheSize(configSource.getCacheSize());
		businessObject.setCachePolicy(configSource.getCachePolicy());
	}

	@Override
	protected Class<CacheConfigImpl> getConfigBeanClass() {
		return CacheConfigImpl.class;
	}

	@Override
	protected void onRollback(finalconfigclasses.bean.BeanUpdateEvent event) {
		System.out.println("  CacheBusinessClassConfigurator: rollback - CacheBusinessComponent left untouched.");
	}
}
