//Source File Name:   weblogic.descriptor.internal.AbstractDescriptorBeanHelper.java
package finalconfigclasses.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import finalconfigclasses.bean.PropertyUpdate;
import finalconfigclasses.bean.misc.BeanUpdateSupport;

//Source File Name:   weblogic.descriptor.internal.AbstractDescriptorBeanHelper.java

/**
 * Class which computes the configBean difference.
 * 
 * This class(and its subclasses) is a good candidate to be an 'inner class' of BaseConfigBean(and its
 * subclasses), because it can directly access private fields of BaseConfigBean. But currently I have
 * organized them separately just for simplicity sake.
 *  
 * TODO: improve readability of this class.
 * TODO: think whether we would use 'Apache Commons BeanUtils' here.
 * TODO: add the ability of calling BeanUpdateListener before applying updates(refer to psp/SomeDevelopement
 * for some implementation).
 */
public class ConfigDiffHelper 
{
	
    protected final ConfigBean bean;
    private ConfigBeanDiff beanDiff;
    //private final BeanUpdateSupport updateSupport;
    
    /** Mine, provide tree like structure for BeanDiff, Corresponding to structure of java beans **/            
    //private BeanInfo beanInfo;    
    private ConfigDiffHelper parent;
    private Map<String, List<ConfigDiffHelper>> childMap = new HashMap<String, List<ConfigDiffHelper>>();
    //private Map<String, PropertyDescriptor> propDescMap = new HashMap<String, PropertyDescriptor>();    
    //private Map<String, Boolean> arrayProcessdMap = new HashMap<String, Boolean>();
    
    /** All subclasses should define this constructor with the same return type and argument list */
    public ConfigDiffHelper(ConfigBean sourceBean/*, BeanUpdateSupport updateSupport*/) {
        this.bean = sourceBean;
        //this.updateSupport = updateSupport;
    }
    
    /** Mine, provide tree like structure for BeanDiff, Corresponding to structure of java beans **/
    public ConfigBeanDiff getBeanDiff() {return beanDiff;}    
    public ConfigDiffHelper getParent() {return parent;}
    private void setParent(ConfigDiffHelper parent) {this.parent = parent;}
    public List<ConfigDiffHelper> getChild(String propertyName) {return childMap.get(propertyName);}
    public Map<String, List<ConfigDiffHelper>> getChildren() {return childMap;}
    private void addChild(String propertyName, ConfigDiffHelper beanHelper) {
    	List<ConfigDiffHelper> list = childMap.get(propertyName);
    	if(list == null) {
    		list = new ArrayList<ConfigDiffHelper>();
    		childMap.put(propertyName, list);
    	}
    	list.add(beanHelper);
    }    
    
    public final void computeDiff(ConfigBean proposedBean)
    {    	
        beanDiff = new ConfigBeanDiff(bean, proposedBean/*, 0, 0*/);
        childMap.clear();
        _computeDiff(proposedBean);
    }

/*    public final boolean hasNonDynamicUpdates() {
    	boolean result = false;
    	result |= beanDiff.hasNonDynamicUpdates();
    	Collection<List<ConfigDiffHelper>> collection = childMap.values();
        for(List<ConfigDiffHelper> list : collection)
	        for(ConfigDiffHelper child : list)
	        	result |= child.hasNonDynamicUpdates();
    	return result;
    }*/
    
    public final void applyUpdate() {
        List<PropertyUpdate> updateList = beanDiff.getUpdateList();
        for(PropertyUpdate propertyUpdate : updateList)
            applyPropertyUpdate(beanDiff, (ConfigPropertyUpdate)propertyUpdate);    	
    }
    /*public final void applyUpdate(boolean saveInFile//, boolean makeThreadSafe)  
    {
        List<PropertyUpdate> updateList = beanDiff.getUpdateList();
        for(PropertyUpdate propertyUpdate : updateList)
            applyPropertyUpdate(beanDiff, (ConfigPropertyUpdate)propertyUpdate//, makeThreadSafe);
        
        //after applying updates now save it in file
        if(saveInFile) {
        	try {
        		bean.save();
        	} catch (ConfigException ce) {
        		System.out.println("Warning, could not save the changes: "+ce);
			}
        }
        
        Collection<List<ConfigDiffHelper>> collection = childMap.values();
        for(List<ConfigDiffHelper> list : collection)
	        for(ConfigDiffHelper child : list)
	        	child.applyUpdate(saveInFile//, makeThreadSafe);
    } */   

    /** Hook for subclass to overwrite default behavior */
    protected void _computeDiff(ConfigBean proposedBean)
    {
        /**
         * The body of this method must be empty; Because
         * method calls from children of this class will
         * reach to this point and this class has a no
         * diff computation but it must not throw exception.
         * It just silently computes nothing.
         */
    }

    /** Hook for subclass to overwrite default behavior */
    protected void applyPropertyUpdate(ConfigBeanUpdateEvent event, ConfigPropertyUpdate update/*, boolean makeThreadSafe*/)
    {
        /**
         * The body of this method must throw exception;
         * It is because if method calls from children of this class
         * could reach to this point then it means no any child could
         * handle such update(maybe a programming mistake). So, here,
         * we must throw exception to report this mistake.
         */
        throw new AssertionError((new StringBuilder()).append("Update fell through: ").append(update).toString());
    }

    protected final void computeDiff(String propertyName, byte srcPropVal, byte proPropVal, boolean isDynamic)
    {
        if(srcPropVal != proPropVal)
            beanDiff.recordChange(propertyName, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, int srcPropVal, int proPropVal, boolean isDynamic)
    {
        if(srcPropVal != proPropVal)
            beanDiff.recordChange(propertyName, isDynamic);
    }

    protected final void computeDiff(String propertyName, long srcPropVal, long proPropVal, boolean isDynamic)
    {
        if(srcPropVal != proPropVal)
            beanDiff.recordChange(propertyName, isDynamic);
    }

    protected final void computeDiff(String propertyName, double srcPropVal, double proPropVal, boolean isDynamic)
    {
        if(srcPropVal != proPropVal)
            beanDiff.recordChange(propertyName, isDynamic);
    }

    protected final void computeDiff(String propertyName, boolean srcPropVal, boolean proPropVal, boolean isDynamic)
    {
        if(srcPropVal != proPropVal)
            beanDiff.recordChange(propertyName, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, char srcPropVal, char proPropVal, boolean isDynamic)
    {
        if(srcPropVal != proPropVal)
            beanDiff.recordChange(propertyName, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, short srcPropVal, short proPropVal, boolean isDynamic)
    {
        if(srcPropVal != proPropVal)
            beanDiff.recordChange(propertyName, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, float srcPropVal, float proPropVal, boolean isDynamic)
    {
        if(srcPropVal != proPropVal)
            beanDiff.recordChange(propertyName, isDynamic);
    }        

    protected final void computeDiff(String propertyName, boolean srcPropValArr[], boolean proPropValArr[], boolean isDynamic)
    {
        if(!Arrays.equals(srcPropValArr, proPropValArr))
            beanDiff.recordChange(propertyName, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, byte srcPropValArr[], byte proPropValArr[], boolean isDynamic)
    {
        if(!Arrays.equals(srcPropValArr, proPropValArr))
            beanDiff.recordChange(propertyName, isDynamic);
    }

    protected final void computeDiff(String propertyName, char srcPropValArr[], char proPropValArr[], boolean isDynamic)
    {
        if(!Arrays.equals(srcPropValArr, proPropValArr))
            beanDiff.recordChange(propertyName, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, short srcPropValArr[], short proPropValArr[], boolean isDynamic)
    {
        if(!Arrays.equals(srcPropValArr, proPropValArr))
            beanDiff.recordChange(propertyName, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, int srcPropValArr[], int proPropValArr[], boolean isDynamic)
    {
        if(!Arrays.equals(srcPropValArr, proPropValArr))
            beanDiff.recordChange(propertyName, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, long srcPropValArr[], long proPropValArr[], boolean isDynamic)
    {
        if(!Arrays.equals(srcPropValArr, proPropValArr))
            beanDiff.recordChange(propertyName, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, float srcPropValArr[], float proPropValArr[], boolean isDynamic)
    {
        if(!Arrays.equals(srcPropValArr, proPropValArr))
            beanDiff.recordChange(propertyName, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, double srcPropValArr[], double proPropValArr[], boolean isDynamic)
    {
        if(!Arrays.equals(srcPropValArr, proPropValArr))
            beanDiff.recordChange(propertyName, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, Object srcPropVal, Object proPropVal, boolean isDynamic)
    {
        computeObjectDiff(propertyName, srcPropVal, proPropVal, false, isDynamic);
    }
    
    protected final void computeDiff(String propertyName, Object srcPropValArr[], Object proPropValArr[], boolean isDynamic)
    {
        computeObjectDiff(propertyName, srcPropValArr, proPropValArr, false, isDynamic, false);
    }

    protected final void computeChildDiff(String propertyName, Object srcPropVal, Object proPropVal)
    {
        computeObjectDiff(propertyName, srcPropVal, proPropVal, true, true);
    }

    protected final void computeChildDiff(String propertyName, Object srcPropVal, Object proPropVal, boolean isDynamic)
    {
        computeObjectDiff(propertyName, srcPropVal, proPropVal, true, isDynamic);
    }

    protected final void computeChildDiff(String propertyName, Object srcPropValArr[], Object proPropValArr[], boolean isDynamic)
    {
        computeObjectDiff(propertyName, srcPropValArr, proPropValArr, true, isDynamic, false);
    }

    protected final void computeChildDiff(String propertyName, Object srcPropValArr[], Object proPropValArr[])
    {
        computeObjectDiff(propertyName, srcPropValArr, proPropValArr, true, true, false);
    }

    protected final void computeDiff(String propertyName, Object srcPropValArr[], Object proPropValArr[], boolean isDynamic, boolean isOrderSensitive/*flag1*/)
    {
        computeObjectDiff(propertyName, srcPropValArr, proPropValArr, false, isDynamic, isOrderSensitive);
    }

    protected final void computeChildDiff(String propertyName, Object srcPropValArr[], Object proPropValArr[], boolean isDynamic, boolean isOrderSensitive/*flag1*/)
    {
        computeObjectDiff(propertyName, srcPropValArr, proPropValArr, true, isDynamic, isOrderSensitive);
    }

    /**
     * <p>
     * This method would compare two objects(obj1, obj2) against each other. Both objects
     * can be from ConfigBean type or simple types like String, Integer,... .
     * </p>
     * <p>
     * The algorithm(when <code>isChildDiff</code> argument is <code>false</code>):
     * <ol>
     * <li> if obj1 == obj2 then two objects are equal(no record on beanDiff). </li>
     *    <ol><li>
     *    else</li>
     *        <ol>
     *        <li>
     *        if one of the obj1 or obj2 is null then property is changed.
     *        </li>
     *        <li>else if obj1 and obj2 are from different types(e.g. one is Integer
     *           and other is ConfigBean) then the property is changed.
     *           </li>
     *        <li>else if obj1 and obj2 are ConfigBeans but with different BeanIDs then
     *           the property is changed.
     *           </li>
     *        <li>else if obj1 and obj2 are ConfigBeans with the same BeanIDs then
     *           two objects are equal(no record on beanDiff).</li>
     *           </ol>
     *     </ol>
     * </ol>                
     * </p>
     */
    private void computeObjectDiff(String propertyName, Object srcPropVal, Object proPropVal, boolean isChildDiff, boolean isDynamic)
    {
        if(srcPropVal != proPropVal)
            if(srcPropVal == null || proPropVal == null)
            {
                if(srcPropVal == null && (proPropVal instanceof ConfigBean) && isChildDiff)
                    beanDiff.recordAddition(propertyName, proPropVal, isDynamic);
                else
                if(proPropVal == null && (srcPropVal instanceof ConfigBean) && isChildDiff)
                    beanDiff.recordRemoval(propertyName, srcPropVal, isDynamic);
                else
                    beanDiff.recordChange(propertyName, isDynamic);
            } else
            if(!haveSameKey(srcPropVal, proPropVal))
            {
                if((srcPropVal instanceof ConfigBean) && (proPropVal instanceof ConfigBean) && isChildDiff)
                {
                    beanDiff.recordRemoval(propertyName, srcPropVal, isDynamic);
                    beanDiff.recordAddition(propertyName, proPropVal, isDynamic);
                } else
                {
                    beanDiff.recordChange(propertyName, isDynamic);
                }
            } else
            if(isChildDiff)
                computeSubDiff(propertyName, srcPropVal, proPropVal);
    }

    /**
     * <p>
     * This method would compare two arrays and any element inside
     * source array which is still exists in proposed array (according
     * to {@link #haveSameKey()} method) would be passed to {@link #computeSubDiff()}
     * method if <code>isChildDiff</code> argument is <code>true</code>. Any other additions
     * or removals just would be recorded as <code>Addition</code> or <code>Removal</code>
     * in BeanDiff object. 
     * </p>
     * <p>
     * Note that this method just assumes array is changed when
     * the proposed array has a same length and has a same elements
     * but just a difference is in the order of elements between
     * source array and proposed array. <code>isOrderSensitive</code> argument
     * also must be <code>true</code> to specify the order is important. If so then
     * this method would call <code>beanDiff.recordChange()</code>. For example
     * suppose the source and proposed array as:
     * <p><blockquote><pre>
     * source array contains  : {e1, e2, e3};
     * proposed array contains: {e2, e3, e1}; 
     * </pre></blockquote></p>
     * Here two arrays are equal but just the order of elements is changed
     * so the beanDiff would contains(if and only if <code>isOrderSensitive</code> is <code>true</code>):
     * <p><blockquote><pre>
     * Change for property 'FooArray'
     * </pre></blockquote></p> 
     * </p>
     * <p>
     * In other cases(e.g. any new element is inserted or old element is
     * removed from proposed then this method just would record additions and
     * removals between two arrays. For example suppose the source and
     * proposed array as:
     * <p><blockquote><pre>
     * source array contains  : {e1, e2, e3};
     * proposed array contains: {e1, e3, e4}; 
     * </pre></blockquote></p>
     * Here 'e2' does not exists in proposed(is removed from source) and 'e4'
     * is newly added to proposed, so the beanDiff would contains:
     * <p><blockquote><pre>
     * Addition for property 'FooArray'  : {e4};
     * Removal  for property 'FooArray'  : {e2};
     * </pre></blockquote></p>
     * In the previous example you can see the method would not 
     * record <code>PropertyUpdate.UpdateType.CHANGE' for array. 
     * </p>
     * @param isOrderSensitive if <code>true</code> then specifies order of elements inside
     *              array is important and if two arrays are equal and
     *              just differ in order of elements then the algorithm
     *              would record the change for the array. If <code>false</code>
     *              then the algorithm would not consider reorder as a change
     *              and assumes both arrays are equals. The client of this
     *              method must specify the order of array is important
     *              or not.
     */        
    @SuppressWarnings("unchecked")
    private final void computeObjectDiff(String propertyName, Object srcPropValArr[], Object proPropValArr[], boolean isChildDiff, boolean isDynamic, boolean isOrderSensitive/*flag2*/)
    {
        boolean flag3 = true;
        ArrayList arraylist = new ArrayList();
        HashMap hashmap = new HashMap();
        int i = 0;
        int j = 0;
        if(srcPropValArr == null)
            srcPropValArr = new Object[0];
        if(proPropValArr == null)
            proPropValArr = new Object[0];
        do
        {
            if(i >= srcPropValArr.length || j >= proPropValArr.length)
                break;
            if(haveSameKey(srcPropValArr[i], proPropValArr[j]))
            {
                if(isChildDiff)
                    computeSubDiff(propertyName, srcPropValArr[i], proPropValArr[j]);
                i++;
                j++;
            } else
            {
                flag3 = false;
                boolean flag4 = false;
                int i1 = Math.min(3, proPropValArr.length - j);
                ArrayList arraylist1 = new ArrayList(i1);
                arraylist1.add(proPropValArr[j]);
                for(int k1 = 1; !flag4 && k1 < i1; k1++)
                    if(haveSameKey(srcPropValArr[i], proPropValArr[j + k1]))
                    {
                        if(isChildDiff)
                            computeSubDiff(propertyName, srcPropValArr[i], proPropValArr[j + k1]);
                        flag4 = true;
                        arraylist.addAll(arraylist1);
                        i++;
                        j += k1 + 1;
                    } else
                    {
                        arraylist1.add(proPropValArr[j + k1]);
                    }

                if(!flag4)
                {
                    int j1 = Math.min(3, srcPropValArr.length - i);
                    ArrayList arraylist2 = new ArrayList(j1);
                    arraylist2.add(srcPropValArr[i]);
                    for(int l1 = 1; !flag4 && l1 < j1; l1++)
                        if(haveSameKey(srcPropValArr[i + l1], proPropValArr[j]))
                        {
                            if(isChildDiff)
                                computeSubDiff(propertyName, srcPropValArr[i + l1], proPropValArr[j]);
                            flag4 = true;
                            Object obj2;
                            for(Iterator iterator3 = arraylist2.iterator(); iterator3.hasNext(); hashmap.put(getKey(obj2), obj2))
                                obj2 = iterator3.next();

                            i += l1 + 1;
                            j++;
                            if(i < srcPropValArr.length)
                                continue;
                            for(; j < proPropValArr.length; j++)
                                arraylist.add(proPropValArr[j]);

                        } else
                        {
                            arraylist2.add(srcPropValArr[i + l1]);
                        }

                }
                if(!flag4)
                {
                    hashmap.put(getKey(srcPropValArr[i]), srcPropValArr[i]);
                    arraylist.add(proPropValArr[j]);
                    i++;
                    j++;
                }
            }
        } while(true);
        if(i < srcPropValArr.length)
        {
            for(int k = i; k < srcPropValArr.length; k++)
            {
                hashmap.put(getKey(srcPropValArr[k]), srcPropValArr[k]);
            }

        } else
        if(j < proPropValArr.length)
        {
            for(int l = j; l < proPropValArr.length; l++)
            {
                arraylist.add(proPropValArr[l]);
            }

        }
        if(arraylist.size() > 0 && hashmap.size() > 0)
        {
            Iterator iterator = arraylist.iterator();
            do
            {
                if(!iterator.hasNext())
                    break;
                Object obj = iterator.next();
                Object obj1 = hashmap.get(getKey(obj));
                if(obj1 != null)
                {
                    hashmap.remove(getKey(obj1));
                    iterator.remove();
                    if(isChildDiff)
                        computeSubDiff(propertyName, obj1, obj);
                }
            } while(true);
        }
        if(isOrderSensitive/*flag2*/ && arraylist.isEmpty() && hashmap.isEmpty() && !flag3)
            beanDiff.recordChange(propertyName, isDynamic);
        for(Iterator iterator1 = arraylist.iterator(); iterator1.hasNext(); beanDiff.recordAddition(propertyName, iterator1.next(), isDynamic));
        for(Iterator iterator2 = hashmap.values().iterator(); iterator2.hasNext(); beanDiff.recordRemoval(propertyName, iterator2.next(), isDynamic));
    }

    protected final void computeSubDiff(String propertyName, Object srcPropVal, Object proPropVal)
    {
        if(srcPropVal instanceof ConfigBean)
        {
        	ConfigBean sourceBean = (ConfigBean)srcPropVal;        	
        	ConfigDiffHelper helper = (ConfigDiffHelper)sourceBean._newDiffHelper();//newConfigDiffHelper(sourceBean);
        	helper.setParent(this);
        	addChild(propertyName, helper);
        	ConfigBean proposedBean = (ConfigBean)proPropVal;
            helper.computeDiff(proposedBean);
        }
    }
    
    
    private static Object getKey(Object obj)
    {
        if(!(obj instanceof ConfigBean))
            return obj;
        else {
        	return ((ConfigBean)obj).getBeanID();
        }
    }

    private boolean haveSameKey(Object obj, Object obj1)
    {
        return getKey(obj).equals(getKey(obj1));
    }
}
