package finalconfigclasses.cfg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.commons.beanutils.PropertyUtils;

import com.thoughtworks.xstream.XStream;

import finalconfigclasses.cfg.misc.SimpleDfsEditStrategy;

/**
 * Singleton class which is central registry of configBeans.
 * 
 * Each application should construct its configmodel(config bean)
 * and then should register it inside this class.
 * 
 * This class supports '.' in logical names of config beans so that we go down in
 * properties by '.' like "PSPEar.jasperReportCacheConfig". we should support it
 * in clone and cloneSubtree methods.
 * 
 * Concurrency considerations:
 * 
 * Any PropertyChangeListener, NodeChangeListener or BeanUpdateListener MUST
 * not initiate a new thread that calls methods of Registry directly or 
 * indirectly. It is because registry would call listeners with its lock held.
 * If listeners violate this constraint dead-lock occurs.
 */
public final class Registry {
	private static class SingletonHolder {
		static final Registry THE_ONE = new Registry();
	}
	
	private final HashMap<String, ConfigBean> map = new HashMap<String, ConfigBean>();	
	private final Object lock = new Object();
	
	private Registry() {}
	
	public static Registry getInstance() {
		return SingletonHolder.THE_ONE;
	}
	
	/**
	 * Creates an XStream instance hardened with the modern type-permission
	 * security framework (mandatory since XStream 1.4.7+; XStream refuses to
	 * (de)serialize anything outside the allowed types otherwise). Config
	 * beans always live under the "finalconfigclasses" package tree, so we
	 * whitelist that plus a handful of common JDK value types used inside
	 * attribute/property maps.
	 */
	private static XStream createXStream() {
		XStream xs = new XStream();
		XStream.setupDefaultSecurity(xs);
		xs.allowTypesByWildcard(new String[] {
				"finalconfigclasses.**",
				"java.lang.*",
				"java.util.*"
		});
		return xs;
	}
	
	/**
	 * @param name the logical name which is associated to configbean.
	 * @param bean the configbean to register.
	 */
	public void putConfig(String name, ConfigBean bean) throws ConfigException {
		synchronized (lock) {
			if (map.containsKey(name))
				throw new ConfigException("Registration with name "+name+" already exists.");
			map.put(name, bean);				
		}
	}
	
	public ConfigBean getConfig(String name) throws ConfigException {
		synchronized (lock) {
			try {
				return (ConfigBean)PropertyUtils.getProperty(map, name);
			} catch (ClassCastException e) {
				throw new ConfigException("Field '"+name+"' is not a ConfigBean.", e);
			} catch (Exception e) {
				throw new ConfigException("Error retrieving field value from bean : " + name, e);
			}
		}
	}
	
	public ConfigBean removeConfig(String name) throws ConfigException {
		synchronized (lock) {
			if(!map.containsKey(name))
				throw new ConfigException("Registration with name "+name+" not found.");
			ConfigBean obj = map.remove(name);
			return obj;
		}
	}
	
	/**
	 * Emits an XML document representing all of the config parameters contained
     * in this config bean and all of its descendants.  This XML document is, in
     * effect, an offline backup of the subtree rooted at the config bean.
     * 
	 * This method is somewhat similar to exportSubtree() method of 
	 * java.util.Preferences class.
	 *  
     * @param name the logical name of source bean.
	 */		
	public void exportSubtree(String name, OutputStream os) throws ConfigException {
		ConfigBean bean = getConfig(name);
		if(bean == null)
			throw new ConfigException("Registration with name "+name+" not found.");
		
		XStream xs = createXStream();
		xs.toXML(bean.cloneSubtree(), os);
	}
	
	/**
	 * Emits an XML document representing all of the config parameters contained
     * in this config bean and all of its descendants.  This XML document is, in
     * effect, an offline backup of the subtree rooted at the config bean.
     * 
	 * This method is somewhat similar to exportSubtree() method of 
	 * java.util.Preferences class.
	 *  
     * @param name the logical name of source bean.
	 */	
	public void exportSubtree(String name, int exportDepth, OutputStream os) throws ConfigException {
		ConfigBean bean = getConfig(name);
		if(bean == null)
			throw new ConfigException("Registration with name "+name+" not found.");
		
		XStream xs = createXStream();
		xs.toXML(bean.cloneSubtree(exportDepth), os);
	}
	
	/**
	 * Emits an XML document representing all of the config parameters contained
     * in this config bean and all of its descendants.  This XML document is, in
     * effect, an offline backup of the subtree rooted at the config bean.
     * 
	 * This method is somewhat similar to exportNode() method of 
	 * java.util.Preferences class.
	 *  
     * @param name the logical name of source bean.
	 */	
	public void exportNode(String name, OutputStream os) throws ConfigException {
		ConfigBean bean = getConfig(name);
		if(bean == null)
			throw new ConfigException("Registration with name "+name+" not found.");
		
		XStream xs = createXStream();
		xs.toXML(bean.clone(), os);
	}
	
	public EditResult importConfig(String name, InputStream clonedVersionXML, final boolean saveInFile) throws ConfigException {
		EditStrategyFactory esf = new EditStrategyFactory() {

			public EditStrategy newEditStrategy(ConfigBean sourceBean,
					ConfigBean proposedBean) {
				EditStrategy editStrategy
					= new SimpleDfsEditStrategy(sourceBean, proposedBean);
				editStrategy.getParameterMap().put(
						Constants.SAVE_IN_FILE,
						saveInFile);
				editStrategy.getParameterMap().put(
						Constants.MAKE_THREAD_SAFE,
						true);				
				return editStrategy;
			}
			
		};
		
		return importConfig(name, clonedVersionXML, esf);		
	}
	
	/**
	 * Imports all of the config bean represented by the XML document on the
     * specified input stream.
     * 
	 * This method is somewhat similar to importPreferences() method of 
	 * java.util.Preferences class.
	 * 
	 * @param name the logical name of source bean.
	 *                  
	 * @param clonedVersionXML the clone of source config in XML representation
	 *                         (the cloned may be modified).
	 * @param saveInFile whether the changes must be persisted into the .properties files.
	 * @return result of edit operation.
	 */	
	public EditResult importConfig(String name, InputStream clonedVersionXML, EditStrategyFactory esf) throws ConfigException {
		synchronized (lock) {
			ConfigBean bean = getConfig(name);
			if(bean == null)
				throw new ConfigException("Registration with name "+name+" not found.");
			
			XStream xs = createXStream();
			ConfigBean clonedVersion = (ConfigBean)xs.fromXML(clonedVersionXML);
			EditStrategy editStrategy
				= esf.newEditStrategy(bean, clonedVersion);		
			editStrategy.computeDiff();
			editStrategy.applyUpdate();	
			return editStrategy.getEditResult();
		}
	}
	
	public String exportSubtree(String name) throws ConfigException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exportSubtree(name, baos);
		return new String(baos.toByteArray());
	}
	
	public String exportSubtree(String name, int exportDepth) throws ConfigException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exportSubtree(name, exportDepth, baos);
		return new String(baos.toByteArray());
	}
	
	public String exportNode(String name) throws ConfigException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exportNode(name, baos);
		return new String(baos.toByteArray());
	}
	
	public EditResult importConfig(String name, String clonedVersionXML, boolean saveInFile) throws ConfigException {
		ByteArrayInputStream bais = new ByteArrayInputStream(clonedVersionXML.getBytes());
		return importConfig(name, bais, saveInFile);
	}
}
