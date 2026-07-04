//Source File Name:   weblogic.descriptor.internal.AbstractDescriptorBean.java
package finalconfigclasses.cfg;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import finalconfigclasses.bean.BeanUpdateListener;
import finalconfigclasses.bean.misc.BeanUpdateSupport;
import finalconfigclasses.cfg.misc.NodeChangeListener;
import finalconfigclasses.cfg.misc.NodeChangeSupport;

//Source File Name:   weblogic.descriptor.internal.AbstractDescriptorBean.java

/**
 * Skeletal implementation of ConfigBean s.  
 */
public abstract class BaseConfigBean implements ConfigBean {
		
	private static final long serialVersionUID = 4538796607883032497L;
	
	/** The unique ID of bean(needed for clone and merge algorithms). */
	private final String beanID;
	/** Contains the value of simple attributes. */
    private final HashMap<String, Object> attr = new HashMap<String, Object>();
    /** Contains the reference to other related ConfigBeans. */
    private final HashMap<String, Object> prop = new HashMap<String, Object>();
    /** Contains whether attribute/property has been explicitly set in this bean. */
	private final HashMap<String, Object> setProp = new HashMap<String, Object>();
	/** Contains default values for attribute/property. */
    private final HashMap<String, Object> defValue;
    /** Whether change to attribute/property is appliable at run-time or needs system restart. */
	private final HashMap<String, Object> dynaProp;
	
	/**
	 * The transient is neccassary for clone process, the XStream would ignore the 
	 * transient fields(needed for transfering configbeans via JMX).
	 */
	private final transient ConfigBean _parent;
	private final transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	private final transient NodeChangeSupport nodeSupport = new NodeChangeSupport(this);
	private final transient BeanUpdateSupport updateSupport = new BeanUpdateSupport(this);
	private volatile transient ReentrantReadWriteLock propertiesLock;
	private final String propertiesFile;
	//private final transient XmlContainer container;
	
	private final String lockID;
	private final String document;
	private final String name;
	private final String keyPrefix;	
	//private boolean _modified;
	
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructors...
    //
    ////////////////////////////////////////////////////////////////////////////
	
	public BaseConfigBean() {
		this(null, null, null, null, null, null, null, null, null);
	}
	
	public BaseConfigBean(final HashMap<String, Object> defValue) {
		this(defValue, createDP(defValue), null, null, null, null, null, null, null);
	}
	
	public BaseConfigBean(final HashMap<String, Object> defValue,
			  			  final HashMap<String, Object> dynaProp) {
		this(defValue, dynaProp, null, null, null, null, null, null, null);
	}
	
	public BaseConfigBean(final HashMap<String, Object> defValue,
			  			  final HashMap<String, Object> dynaProp,
			  			  final ConfigBean parent) {
		this(defValue, dynaProp, parent, null, null, null, null, null, null);
	}

	public BaseConfigBean(final HashMap<String, Object> defValue,
						  final String propertiesFile,
			  			  /*final XmlContainer container,*/
			  			  final String lockID,
			  			  final ReentrantReadWriteLock propertiesLock,
			  			  final String document,
			  			  final String name,
			  			  final String keyPrefix) {
		this(defValue, createDP(defValue), null, propertiesFile, lockID, propertiesLock, document, name, keyPrefix);
	}
	
	public BaseConfigBean(final HashMap<String, Object> defValue,
						  final ConfigBean parent,
						  final String propertiesFile,
						  /*final XmlContainer container,*/
						  final String lockID,
			  			  final ReentrantReadWriteLock propertiesLock,
			  			  final String document,
			  			  final String name,
			  			  final String keyPrefix) {
		this(defValue, createDP(defValue), parent, propertiesFile, lockID, propertiesLock, document, name, keyPrefix);
	}
	
	public BaseConfigBean(final HashMap<String, Object> defValue,
			  			  final HashMap<String, Object> dynaProp,
			  			  final String propertiesFile,
			  			  /*final XmlContainer container,*/
			  			  final String lockID,
			  			  final ReentrantReadWriteLock propertiesLock,
			  			  final String document,
			  			  final String name,
			  			  final String keyPrefix) {
		this(defValue, dynaProp, null, propertiesFile, lockID, propertiesLock, document, name, keyPrefix);
	}
	
	public BaseConfigBean(final HashMap<String, Object> defValue,
						  final HashMap<String, Object> dynaProp,
						  final ConfigBean parent,
						  final String propertiesFile,
						  /*final XmlContainer container,*/
						  final String lockID,
						  final ReentrantReadWriteLock propertiesLock,
						  final String document,
						  final String name,
						  final String keyPrefix) {
		this(UUID.randomUUID().toString(),
		     defValue, dynaProp, parent, propertiesFile, lockID, propertiesLock, document, name, keyPrefix);
	}

	//full constructor which should be defined in all subclasses(the correct
	//behavior of clone() method is dependent to this constructor)
	public BaseConfigBean(final String beanID,
						  final HashMap<String, Object> defValue,
						  final HashMap<String, Object> dynaProp,
						  final ConfigBean parent,
						  final String propertiesFile,
						  /*final XmlContainer container,*/
						  final String lockID,
						  final ReentrantReadWriteLock propertiesLock,
						  final String document,
						  final String name,
						  final String keyPrefix) {
		if(beanID == null)
    		throw new IllegalArgumentException("BeanID should not be null.");
    	this.beanID = beanID;
		this.defValue = defValue;	
		//setting all properties to their defualt values
		if(defValue != null)
			_getAttr().putAll(defValue);
		this._parent = parent;
		this.dynaProp = dynaProp;
		//this.container = container;
		if(propertiesLock != null) {
			if(lockID == null)
				throw new IllegalArgumentException("LockID should not be null.");
		}
		this.lockID = lockID;
		this.propertiesLock = propertiesLock;
		this.propertiesFile = propertiesFile;
		this.document = document;
		this.name = name;
		this.keyPrefix = keyPrefix;
	}
	
    ////////////////////////////////////////////////////////////////////////////
    //
    // Attribute and property getters/setters...
    //
    ////////////////////////////////////////////////////////////////////////////
	
    protected final Object getAttr(String name) {
    	/*if(_getIsWriteOnly())
			throw new UnsupportedOperationException("The data is write only.");*/    	
    	return _getAttr().get(name);
    }
    
    protected final void setAttr(String name, Object value) {
    	/*if(_getIsReadOnly())
    		throw new UnsupportedOperationException("The data is read only.");*/
    	_getAttr().put(name, value);
    }
    
    protected final Object getProp(String name) {
    	/*if(_getIsWriteOnly())
			throw new UnsupportedOperationException("The data is write only.");*/    	    	
    	return _getProp().get(name);
    }
    
    protected final void setProp(String name, Object value) {
    	/*if(_getIsReadOnly())
    		throw new UnsupportedOperationException("The data is read only.");*/    	
    	_getProp().put(name, value);
    }
    
	public final Object getDefValue(String propertyName) {
		/*readLock();
		try {*/
		if (defValue.containsKey(propertyName))
            return defValue.get(propertyName);
        throw new AssertionError("Unknown property '"+propertyName+"'. Can not find its default value in map.");
		/*} finally {
			readUnlock();
		}*/
	}
	
    public final boolean isDynamic(String propertyName) {
    	/*readLock();
    	try {*/
        if (dynaProp.containsKey(propertyName))
            return (Boolean)dynaProp.get(propertyName);
        throw new AssertionError("Unknown property '" + propertyName + "'. Can not find its dynamic flag in map.");
    	/*} finally {
    		readUnlock();
    	}*/
    }
    
	public final void unSet(String propertyName) throws IllegalArgumentException {
		Object[] objArr = null;
		writeLock();
		try {
		objArr = _unSet(propertyName);
		} finally {
			writeUnlock();
		}
		//calling listeners out of lock block to avoid dead-lock.
		firePropertyChange(propertyName, objArr[0], objArr[1]);
	}
	
	public final boolean isSet(String propertyName) throws IllegalArgumentException {
		readLock();
		try {
		return _isSet(propertyName);
		} finally {
			readUnlock();
		}
	}
	
    ////////////////////////////////////////////////////////////////////////////
    //
    // Hashmap getters/setters...
    //
    ////////////////////////////////////////////////////////////////////////////
    
    protected final HashMap<String, Object> _getAttr() {
    	return attr;
    }
    
    protected final HashMap<String, Object> _getProp() {
    	return prop;
    }
    
    protected final HashMap<String, Object> _getDefValue() {
    	return defValue;
    }
    
    protected final HashMap<String, Object> _getSetProp() {
    	return setProp;
    }
    
    protected final HashMap<String, Object> _getDynaProp() {
    	return dynaProp;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //
    // Lock/UnLock methods...
    //
    ////////////////////////////////////////////////////////////////////////////
    
    protected final void readLock() {
    	ReentrantReadWriteLock rrwl = propertiesLock;
    	if(rrwl != null)
    		rrwl.readLock().lock();
    }
    
    protected final void readUnlock() {
    	ReentrantReadWriteLock rrwl = propertiesLock;
    	if(rrwl != null)
    		rrwl.readLock().unlock();
    }

    protected final void writeLock() {
    	ReentrantReadWriteLock rrwl = propertiesLock;
    	if(rrwl != null)
    		rrwl.writeLock().lock();
    }
    
    protected final void writeUnlock() {
    	ReentrantReadWriteLock rrwl = propertiesLock;
    	if(rrwl != null)
    		rrwl.writeLock().unlock();
    }
    
	public final String _getLockID() {
		return lockID;
	}
	
    ////////////////////////////////////////////////////////////////////////////
    //
    // Clone methods...
    //
    ////////////////////////////////////////////////////////////////////////////
    
    public final ConfigBean clone() {
    	readLock();
    	try {
    	return _clone(null, 0);
    	} finally {
    		readUnlock();
    	}
    }
    
    public final ConfigBean clone(final ConfigBean parentOfCloned) {
    	readLock();
    	try {
    	return _clone(parentOfCloned, 0);
    	} finally {
    		readUnlock();
    	}
    }
    
    public final ConfigBean cloneSubtree() {
    	readLock();
    	try {
    	return _clone(null, Integer.MAX_VALUE);
    	} finally {
    		readUnlock();
    	}
    }
    
    public final ConfigBean cloneSubtree(final ConfigBean parentOfCloned) {
    	readLock();
    	try {
    	return _clone(parentOfCloned, Integer.MAX_VALUE);
    	} finally {
    		readUnlock();
    	}
    }
	
    public final ConfigBean cloneSubtree(int cloneDepth) {
    	readLock();
    	try {
    	return _clone(null, cloneDepth);
    	} finally {
    		readUnlock();
    	}
    }
    
    public final ConfigBean cloneSubtree(final ConfigBean parentOfCloned, int cloneDepth) {
    	readLock();
    	try {
    	return _clone(parentOfCloned, cloneDepth);
    	} finally {
    		readUnlock();
    	}
    }
    
    @SuppressWarnings("unchecked")
    protected final ConfigBean _clone(final ConfigBean parentOfCloned, int cloneDepth) {
    	BaseConfigBean cloneObj = null;
		try {
			if(parentOfCloned != null && parentOfCloned == this) {
				System.out.println("The parent of config bean can not be the bean itself!");
				return null;
			}
			//using full constructor to instantiate cloned object...
			Constructor c = getClass().getConstructor(new Class[] {String.class, HashMap.class, HashMap.class,
					ConfigBean.class, String.class, String.class, ReentrantReadWriteLock.class, String.class, String.class, String.class});
			HashMap<String, Object> cloneDefValue = null;
			if(_getDefValue() != null)
				cloneDefValue = (HashMap<String, Object>)_getDefValue().clone();
			HashMap<String, Boolean> cloneDynaProp = null;
			if(dynaProp != null)
				cloneDynaProp = (HashMap<String, Boolean>)dynaProp.clone(); 
			cloneObj = (BaseConfigBean)c.newInstance(_getBeanID(), cloneDefValue,
					cloneDynaProp,
					parentOfCloned,
					propertiesFile,
					lockID,
					null,//we do not place locks on cloned version
					document,
					name,
					keyPrefix);
			
			cloneObj._getSetProp().putAll((HashMap<String, Object>)_getSetProp());
			
			//processing Attr map
			//cloneObj._getAttr().putAll(_getAttr());			
			for(Map.Entry<String, Object> ent : _getAttr().entrySet()) {
				String key = ent.getKey();
				Object obj = ent.getValue();
				if(obj == null) {
					cloneObj._getAttr().put(key, null);
				} else {
					if(obj.getClass().isArray()) {
						int length = Array.getLength(obj);
						Object clonedArr = Array.newInstance(obj.getClass().getComponentType(), length);
						for(int i = 0 ; i < length; i++) {
							Array.set(clonedArr, i, Array.get(obj, i));
						}
						cloneObj._getAttr().put(key, clonedArr);
					} else {
						cloneObj._getAttr().put(key, obj);
					}
				}
			}
			
			//processing Prop map			
			if(cloneDepth > 0) {
				for(Map.Entry<String, Object> ent : _getProp().entrySet()) {
					String key = ent.getKey();
					Object obj = ent.getValue();
					if(obj == null) {
						cloneObj._getProp().put(key, null);
					} else if(obj instanceof ConfigBean) {
						ConfigBean cb = (ConfigBean) obj;
						if(cb == this) {
							System.out.println("Warining: loop in config bean "+this+" for prop "+key+", ignoring...");
							continue;
						}
						if(cb._getParent() == null) {
							ConfigBean clonedCb = cb.cloneSubtree(cloneDepth - 1);
							cloneObj._getProp().put(key, clonedCb);
						} else if(cb._getParent().equals(this)) {
							ConfigBean clonedCb = cb.cloneSubtree(cloneObj, cloneDepth - 1);
							cloneObj._getProp().put(key, clonedCb);
						} else {
							System.out.println("Warning: could not find proper parent for prop "+key+", ignoring...");
						}
					} else if(obj instanceof ConfigBean[]) {
						ConfigBean[] cbArr = (ConfigBean[]) obj;						
						ArrayList<ConfigBean> list = new ArrayList<ConfigBean>();
						for(ConfigBean cb : cbArr) {
							if(cb == this) {
								System.out.println("Warining: loop in config bean "+this+" for prop "+key+", ignoring...");
								continue;
							}
							if(cb._getParent() == null) {
								ConfigBean clonedCb = cb.cloneSubtree(cloneDepth - 1);
								list.add(clonedCb);
							} else if(cb._getParent().equals(this)) {
								ConfigBean clonedCb = cb.cloneSubtree(cloneObj, cloneDepth - 1);
								list.add(clonedCb);
							} else {
								System.out.println("Warning: could not find proper parent for prop "+key+", ignoring...");
							}
						}
						Object clonedCbArr = Array.newInstance(obj.getClass().getComponentType(), list.size());
						for(int i = 0 ; i < list.size(); i++)
							Array.set(clonedCbArr, i, list.get(i));
						cloneObj._getProp().put(key, clonedCbArr);
					}
				}
			}
			
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
        return cloneObj;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //
    // Remaining methods...
    //
    ////////////////////////////////////////////////////////////////////////////
    
	protected final boolean _isSet(String propertyName) {
		Boolean b = (Boolean)setProp.get(propertyName);
		return b == null ? false : b;
	}
	
    public final void _conditionalUnset(boolean isUnsetUpdate, String propertyName) {
    	Object[] objArr = null;
    	writeLock();
    	try {
    	if(isUnsetUpdate)
            objArr = _unSet(propertyName);
    	} finally {
    		writeUnlock();
    	}
    	//calling listeners out of lock block to avoid dead-lock.
    	if(isUnsetUpdate)
    		firePropertyChange(propertyName, objArr[0], objArr[1]);
    }
    
	protected final void _markSet(String propertyName, boolean set) {
        /*
         * Difference between this method and _loadMarkSet() method is
         * that this method is designed for setCacheSize/unset methods
         * and will set modification field to true. Modification state
         * will show the user changes to config object.
         */		
		if (set) 
			setProp.put(propertyName, Boolean.TRUE);
		else
			setProp.put(propertyName, null);
		//_setModified(true);
	}
	
	protected final void _loadMarkSet(String propertyName, boolean set) {
        /*
         * Difference between this method and _markSet() method is
         * that this method is designed for load process and will NOT
         * set modification field to true. Modification state is NOT
         * for load() method changes on config object.
         */
		if (set) 
			setProp.put(propertyName, Boolean.TRUE);
		else
			setProp.put(propertyName, null);
	}
	
	protected final void _postSet(String propertyName, Object oldValue, Object newValue) {
		_markSet(propertyName, true);
	}
	
	protected final Object[] _unSet(String propertyName) {
		Object defVal = getDefValue(propertyName);
		Object oldVal = getAttr(propertyName);
		setAttr(propertyName, defVal);
		_markSet(propertyName, false);
		return new Object[] {oldVal, defVal};
	}
	
	protected final void firePropertyChange(String propertyName, Object oldVal,
			Object newVal) {
		 /* The if is necessary, it is because that a changeSupport is transient and
		 * you may call _postSet or unSet on a cloned version(which its changeSupport field is null).
		 * So the code should behavie as expected(it should not throw NullPointerException here).
		 */		
		if (changeSupport != null)
			changeSupport.firePropertyChange(propertyName, oldVal,
					newVal);
	}
	
	protected final void fireIndexedPropertyChange(String propertyName, int index,
			  Object oldVal, Object newVal) {
		 /* The if is necessary, it is because that a changeSupport is transient and
		 * you may call _postSet or unSet on a cloned version(which its changeSupport field is null).
		 * So the code should behavie as expected(it should not throw NullPointerException here).
		 */		
		if (changeSupport != null)
			changeSupport.fireIndexedPropertyChange(propertyName, index, oldVal,
					newVal);
	}
	
	protected final void fireNodeChange(String propertyName, Object oldVal,
			Object newVal) {
		 /* The if is necessary, it is because that a nodeSupport is transient and
		 * you may call _postSet or unSet on a cloned version(which its nodeSupport field is null).
		 * So the code should behavie as expected(it should not throw NullPointerException here).
		 */		
		if (nodeSupport != null)
			nodeSupport.fireNodeChange(propertyName, oldVal,
					newVal);
	}
	
	protected final void fireIndexedNodeChange(String propertyName, int index,
			Object oldVal, Object newVal) {
		 /* The if is necessary, it is because that a nodeSupport is transient and
		 * you may call _postSet or unSet on a cloned version(which its nodeSupport field is null).
		 * So the code should behavie as expected(it should not throw NullPointerException here).
		 */		
		if (nodeSupport != null)
			nodeSupport.fireIndexedNodeChange(propertyName, index, oldVal,
					newVal);
	}	
	
	/*public boolean _isModified() {
		return _modified;
	}

	public void _setModified(boolean modified) {
		this._modified = modified;
	}*/
    
	public boolean needsSubdiff(String propertyName) {
		readLock();
		try {
		Object obj = getProp(propertyName);
    	return (obj instanceof ConfigBean) || (obj instanceof ConfigBean[]);
		} finally {
			readUnlock();
		}
	}
	
	public ConfigDiffHelper _newDiffHelper() {
		ConfigDiffHelper diffHelper = new ConfigDiffHelper(this/*, updateSupport*/);
		return diffHelper;
		//return "finalconfigclasses.cfg.ConfigDiffHelper";
	}
    
	public final void addPropertyChangeListener(
			PropertyChangeListener propertychangelistener) {
		changeSupport.addPropertyChangeListener(propertychangelistener);
	}

	public final void removePropertyChangeListener(
			PropertyChangeListener propertychangelistener) {
		changeSupport.removePropertyChangeListener(propertychangelistener);
	}

	public final PropertyChangeListener[] getPropertyChangeListeners() {
	   return changeSupport.getPropertyChangeListeners();
	}
		
    public final void addNodeChangeListener(NodeChangeListener nodechangelistener) {
    	nodeSupport.addNodeChangeListener(nodechangelistener);
	}

	public final void removeNodeChangeListener(NodeChangeListener nodechangelistener) {
		nodeSupport.removeNodeChangeListener(nodechangelistener);
	}
	
	public final NodeChangeListener[] getNodeChangeListeners() {
		return nodeSupport.getNodeChangeListeners();
	}	

	public final void addBeanUpdateListener(BeanUpdateListener beanupdatelistener) {
    	updateSupport.addBeanUpdateListener(beanupdatelistener);
    }

	public final void removeBeanUpdateListener(BeanUpdateListener beanupdatelistener) {
		updateSupport.removeBeanUpdateListener(beanupdatelistener);
	}

	public final BeanUpdateListener[] getBeanUpdateListeners() {
		return updateSupport.getBeanUpdateListeners();
	}
	
	public void accept(ConfigBeanVisitor visitor) throws Exception {
        if (visitor == null) {
            throw new IllegalArgumentException("Visitor must not be null!");
        }
        if (!visitor.terminate()) {
            visitor.visitBeforeChildren(this);
            
            HashMap<String, Object> snapshot = null;            
            readLock();
    		try {    			
    			snapshot = new HashMap<String, Object>(_getProp());
    		} finally {
    			readUnlock();
    		}
    		Collection<Object> values = snapshot.values();
    		for(Object obj : values) {
			    if(obj instanceof ConfigBean) {
				    ((ConfigBean) obj).accept(visitor);
			    } else if(obj instanceof ConfigBean[]) {
				    ConfigBean[] arrCopy;
				    readLock();
	    		    try {
	    			    arrCopy = (ConfigBean[])(((ConfigBean[])obj).clone());
	    		    } finally {
	    			    readUnlock();
	    		    }
				    for(ConfigBean cb : arrCopy) {
					    cb.accept(visitor);
				    }
			    }
    		}

            visitor.visitAfterChildren(this);
        }
	}
	
	public abstract String _getXPath();
	
	public String _getBeanID() {
		return beanID;
	}
	
	public Class<?> _getBeanClass() {
		return getClass();
	}
	
	public final ConfigBean _getParent() {
		return _parent;
	}

	public final ReentrantReadWriteLock _getPropertiesLock() {
		return propertiesLock;
	}

	public final void _setPropertiesLock(ReentrantReadWriteLock propertiesLock) {
		this.propertiesLock = propertiesLock;
	}
	
	protected final BeanUpdateSupport _getUpdateSupport() {
		return updateSupport;
	}
	
    public final String _getPropertiesFile() {
	    return propertiesFile;
    }	
//	protected final XmlContainer _getXmlContainer() {
//		return container;
//	}
	
	public final String _getDocument() {
		return document;
	}
	
	protected final String _getName() {
		return name;
	}
	
	protected final String _getKeyPrefix() {
		return keyPrefix;
	}
	
    ////////////////////////////////////////////////////////////////////////////
    //
    // Load/Save methods...
    //
    ////////////////////////////////////////////////////////////////////////////

	//every child must overwrite this method and must not use parent version
	public abstract void load() throws ConfigException;

	//every child must overwrite this method and must not use parent version
	public abstract void save() throws ConfigException;
	
    ////////////////////////////////////////////////////////////////////////////
    //
    // Utility and Helper methods...
    //
    ////////////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked")
	protected static final ArrayList arrayToList(Object array) {
		ArrayList list = new ArrayList();
		if(array != null) {
			int length = Array.getLength(array);
			for(int i = 0; i < length; i++)
				list.add(Array.get(array, i));
		}
		return list;
	}
	
	protected static final Object listToArray(ArrayList list, Class componentType) {
		Object arr = Array.newInstance(componentType, list.size());
		for(int i = 0; i < list.size(); i++)
			Array.set(arr, i, list.get(i));
		return arr;
	}
	
	//this methods works by 'equality of references' not 'equality of values'.
	protected static final <T> int indexOf(ArrayList<T> list, T obj) {
		int idx = -1;		
		for(int i = 0; i < list.size(); i++) {
			T elem = list.get(i);			
			if(elem == obj) {//reference equality...
				idx = i;
				break;
			}
		}
		return idx;
	}
	
	protected static final String eval(String localLocPrefix, String ct, String name) {
		StringBuilder sb = new StringBuilder();		
		if(localLocPrefix != null) {
			sb.append(localLocPrefix);
			sb.append('/');
		}
		sb.append(ct);
		if(name != null) {
			sb.append("[@name='");
			sb.append(name);
			sb.append("']");
		}
		return sb.toString();
	}
	
	protected static final Object typedArr(String[] arr, Class type) {
		if(boolean.class.equals(type)) {
			boolean[] result = new boolean[arr.length];
			for(int i = 0; i < arr.length; i++)
				result[i] = Boolean.parseBoolean(arr[i]);
			return result;
		}
		if(char.class.equals(type)) {
			char[] result = new char[arr.length];
			for(int i = 0; i < arr.length; i++)
				result[i] = arr[i].charAt(0);
			return result;			
		}
//		if("byte".equals(type)) {
//				return "getByte";
//		}
		if(short.class.equals(type)) {
			short[] result = new short[arr.length];
			for(int i = 0; i < arr.length; i++)
				result[i] = Short.parseShort(arr[i]);
			return result;			
		}
		if(int.class.equals(type)) {
			int[] result = new int[arr.length];
			for(int i = 0; i < arr.length; i++)
				result[i] = Integer.parseInt(arr[i]);
			return result;			
		}
		if(long.class.equals(type)) {
			long[] result = new long[arr.length];
			for(int i = 0; i < arr.length; i++)
				result[i] = Long.parseLong(arr[i]);
			return result;	
		}
		if(float.class.equals(type)) {
			float[] result = new float[arr.length];
			for(int i = 0; i < arr.length; i++)
				result[i] = Float.parseFloat(arr[i]);
			return result;
		}
		if(double.class.equals(type)) {
			double[] result = new double[arr.length];
			for(int i = 0; i < arr.length; i++)
				result[i] = Double.parseDouble(arr[i]);
			return result;
		}
		///////////////////////////////////
		if(Boolean.class.equals(type)) {
			Boolean[] result = new Boolean[arr.length];
			for(int i = 0; i < arr.length; i++)
				if(arr[i] != null)
					result[i] = Boolean.valueOf(arr[i]);
			return result;
		}
		if(Character.class.equals(type)) {
			Character[] result = new Character[arr.length];
			for(int i = 0; i < arr.length; i++)
				if(arr[i] != null)
					result[i] = Character.valueOf(arr[i].charAt(0));
			return result;			
		}
//		if("byte".equals(type)
//				|| "Byte".equals(type)) {
//				return "getByte";
//		}
		if(Short.class.equals(type)) {
			Short[] result = new Short[arr.length];
			for(int i = 0; i < arr.length; i++)
				if(arr[i] != null)
					result[i] = Short.valueOf(arr[i]);
			return result;			
		}
		if(Integer.class.equals(type)) {
			Integer[] result = new Integer[arr.length];
			for(int i = 0; i < arr.length; i++)
				if (arr[i] != null)
					result[i] = Integer.valueOf(arr[i]);
			return result;			
		}
		if(Long.class.equals(type)) {
			Long[] result = new Long[arr.length];
			for(int i = 0; i < arr.length; i++)
				if (arr[i] != null)
					result[i] = Long.valueOf(arr[i]);
			return result;	
		}
		if(Float.class.equals(type)) {
			Float[] result = new Float[arr.length];
			for(int i = 0; i < arr.length; i++)
				if (arr[i] != null)
					result[i] = Float.valueOf(arr[i]);
			return result;
		}
		if(Double.class.equals(type)) {
			Double[] result = new Double[arr.length];
			for(int i = 0; i < arr.length; i++)
				if (arr[i] != null)
					result[i] = Double.valueOf(arr[i]);
			return result;
		}		
		if(String.class.equals(type)) {
			return arr;
		}
		return null;
	}
	
	private static final HashMap<String, Object> createDP(final HashMap<String, Object> defValue) {
		if(defValue == null)
			return null;
		HashMap<String, Object> result = new HashMap<String, Object>();
		for(String key : defValue.keySet()) {
			//all properties are dynamic
			result.put(key, Boolean.TRUE);
		}
		return result;
	}
}
