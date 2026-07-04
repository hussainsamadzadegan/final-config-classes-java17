package finalconfigclasses.cfg.gen.demo;

/**
 * A stand-in for some real, already-existing business component in your app
 * (an in-memory cache implementation, in this example). It deliberately has
 * <b>no dependency whatsoever</b> on {@code finalconfigclasses.cfg.*} - not
 * even on {@code ConfigBean}. It just declares its own {@code cacheSize}/
 * {@code cachePolicy} fields and setters, exactly as it would if this
 * project had no config framework at all.
 *
 * This duplication of field names between {@code CacheBusinessComponent}
 * and {@code CacheConfigImpl} is intentional: it's what keeps the two
 * decoupled. Nothing here changes if the config framework, its storage
 * backend, or even the specific config bean class backing it, changes -
 * only {@link CacheBusinessClassConfigurator} needs to know about both
 * sides.
 */
public class CacheBusinessComponent {

	private int cacheSize;
	private String cachePolicy;

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		System.out.println("  CacheBusinessComponent: cacheSize -> " + cacheSize);
		this.cacheSize = cacheSize;
	}

	public String getCachePolicy() {
		return cachePolicy;
	}

	public void setCachePolicy(String cachePolicy) {
		System.out.println("  CacheBusinessComponent: cachePolicy -> " + cachePolicy);
		this.cachePolicy = cachePolicy;
	}
}
