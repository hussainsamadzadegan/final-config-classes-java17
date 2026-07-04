package finalconfigclasses.cfg;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import finalconfigclasses.bean.BeanUpdateListener;
import finalconfigclasses.bean.DiffableBean;
import finalconfigclasses.bean.SettableBean;
import finalconfigclasses.cfg.misc.NodeChangeListener;

/**
 * The general interface of all config classes.
 * This interface defines a tree like structure for configuration data(i.e. each
 * config object has a parent).
 * 
 * The interface which we would design our classes with it.
 * All class running in local JVM work with this interface.
 * 
 * The addPropertyChangeListener and removePropertyChangeListener methods
 * are available to local objects, they are not available for remote objects so
 * we extract them out from the ConfigMBean interface.
 * 
 * This interface and all implementations of it should be serializable because
 * we want to transfer the config beans to UI(through the JMX - clone method of ConfigMBean).
 */
public interface ConfigBean extends DiffableBean, SettableBean, Cloneable, Serializable {
	
	public ConfigBean _getParent();

	public String _getXPath();

	/**
	 * The root path (ZooKeeper path prefix, e.g. "/app/config") under which
	 * this config bean's subtree is persisted. Used by the ZooKeeper backed
	 * load()/save() implementations and by {@link finalconfigclasses.cfg.zk.ZkConfigManager}
	 * to compute the fully qualified znode path for this bean.
	 */
	public String _getPropertiesFile();

	/**
	 * The logical "document" (a sub-namespace under the properties root, e.g.
	 * distinguishing several independent config trees stored under the same
	 * ZooKeeper root) that this config bean belongs to. May be {@code null}.
	 */
	public String _getDocument();

	public String _getLockID();
	
	public ConfigDiffHelper _newDiffHelper();
	
	public ReentrantReadWriteLock _getPropertiesLock();
	
	public void _setPropertiesLock(ReentrantReadWriteLock propertiesLock);
	
    /**
     * Listeners which are just interested in 'attr changes'.
     */		
    public void addPropertyChangeListener(PropertyChangeListener listener);
    public void removePropertyChangeListener(PropertyChangeListener listener);   
    public PropertyChangeListener[] getPropertyChangeListeners();

    /**
     * Listeners which are just interested in 'prop change'(i.e.
     * 'relation changes').
     */     
	public void addNodeChangeListener(NodeChangeListener listener);	
	public void removeNodeChangeListener(NodeChangeListener listener);
	public NodeChangeListener[] getNodeChangeListeners();    
    
    /**
     * Listeners which are interested in 'batch validation' of
     * changes.
     */
    public void addBeanUpdateListener(BeanUpdateListener listener);
	public void removeBeanUpdateListener(BeanUpdateListener listener);
	public BeanUpdateListener[] getBeanUpdateListeners();
	
	public void _conditionalUnset(boolean isUnsetUpdate, String propertyName) throws IllegalArgumentException;
	
	/**
	 * shallow clone.
	 */
    public ConfigBean clone();

	/**
	 * shallow clone.
	 */
    public ConfigBean clone(ConfigBean parentOfCloned);
	
	/**
	 * deep clone(all descendants would be cloned).
	 */
    public ConfigBean cloneSubtree();
    
	/**
	 * deep clone(all descendants would be cloned).
	 */
    public ConfigBean cloneSubtree(ConfigBean parentOfCloned);
	
	/**
	 * This method is useful for UI.
	 * if you set the cloneDepth to zero then you
	 * would get the result of clone() method.
	 */
    public ConfigBean cloneSubtree(int cloneDepth);
    
	/**
	 * This method is useful for UI.
	 * if you set the cloneDepth to zero then you
	 * would get the result of clone() method.
	 */
    public ConfigBean cloneSubtree(ConfigBean parentOfCloned, int cloneDepth);
    
    /**
     * Visits this config bean and all its sub beans(related beans). This method provides a simple
     * means for going through a hierarchical structure of configuration beans.
     *
     * @see ConfigBeanVisitor
     * @param visitor the visitor
     */    
    public void accept(ConfigBeanVisitor visitor) throws Exception;
    
    /**
     * Listeners which are just interested in 'property change'.
     * 
     * Think about following methods. If following methods present
     * then they provide flexible way to set and get Attrs/Props. They
     * provide simple way for <code>ConfigDiffHelper</code> to set/get
     * Attrs/Props without using <code>PropertyDescriptors</code> and reflection.
     * Alos by providing these methods you can register ConfigBeans as
     * DynamicBeans.
     * 
     * Think which of these method must be inside ConfigMBean interface
     * for remote access.
     */    
    //	public Object getAttr(String name) throws IllegalArgumentException;
    //	public void setAttr(String name, Object value) throws IllegalArgumentException;
    //	public Object getProp(String name) throws IllegalArgumentException;
    //	public void setProp(String name, Object value) throws IllegalArgumentException;    
    
	/**
	 * Loads the config bean from its backing store(e.g. from properties file).
	 */
	public void load() throws ConfigException;
	
	/**
	 * Saves the config bean into backing store.
	 */
	public void save() throws ConfigException;
	
	/**
	 * Returns the default value of property.
	 * This method lets the UI to provide the 'Restore to defaults' functionality.
	 * 
	 * UI would clones the config bean and then writes the
	 * default values into cloned version and then writes back
	 * the cloned version on config bean.
	 */
	public Object getDefValue(String propertyName) throws IllegalArgumentException;
	
	/**
	 * Specifies whether the property change is appliable at runtime(the
	 * property is dynamic) or needs the system restart(static property).
	 */
	public boolean isDynamic(String propertyName) throws IllegalArgumentException;    
}