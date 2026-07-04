package finalconfigclasses.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import finalconfigclasses.bean.PropertyUpdate;
import finalconfigclasses.bean.PropertyUpdate.UpdateType;

/**
 * Class which models beanDifference between two confg beans.
 */
public class ConfigBeanDiff extends ConfigBeanUpdateEvent {
	
	private static final long serialVersionUID = 8269469110029705596L;
	
	private Set<ConfigPropertyUpdate> updateSet;
    private ArrayList<ConfigPropertyUpdate> updateList;
    /*private int beanDiffID;*/
    private boolean hasNonDynamicUpdates;
    
    @SuppressWarnings("unchecked")
	public ConfigBeanDiff(ConfigBean sourceBean, ConfigBean proposedBean/*, int updateID, int beanDiffID*/)
    {
        super(sourceBean, proposedBean/*, updateID*/);        
        updateSet = Collections.EMPTY_SET;
        /*this.beanDiffID = beanDiffID;*/
    }

    public void recordChange(String propertyName, boolean isDynamic)
    {
        addUpdate(new ConfigPropertyUpdate(
        		propertyName,
        		UpdateType.CHANGE,
        		null,
        		isDynamic,
        		getSourceBean().isSet(propertyName),
        		getProposedBean().isSet(propertyName)));
        checkAndSetNonDynamicUpdates(isDynamic);
    }

    public void recordRemoval(String propertyName, Object removedObj, boolean isDynamic)
    {
        addUpdate(new ConfigPropertyUpdate(
        		propertyName,
        		UpdateType.REMOVE,
        		removedObj,
        		isDynamic,
        		getSourceBean().isSet(propertyName),
        		getProposedBean().isSet(propertyName)));
        checkAndSetNonDynamicUpdates(isDynamic);
    }

    public void recordAddition(String propertyName, Object addedObj, boolean isDynamic)
    {
        addUpdate(new ConfigPropertyUpdate(
        		propertyName,
        		UpdateType.ADD,
        		addedObj,
        		isDynamic,
        		getSourceBean().isSet(propertyName),
        		getProposedBean().isSet(propertyName)));
        checkAndSetNonDynamicUpdates(isDynamic);
    }

    public int size()
    {
        return updateSet.size();
    }

    @SuppressWarnings("unchecked")
    public List<PropertyUpdate> getUpdateList()
    {
        if(updateList == null) {
        	updateList = new ArrayList<ConfigPropertyUpdate>();
        	updateList.addAll(updateSet);
        }
        return (List)updateList;
    }

    private void checkAndSetNonDynamicUpdates(boolean isDynamic)
    {
        if(!isDynamic && !hasNonDynamicUpdates)
            hasNonDynamicUpdates = true;
    }

    public boolean hasNonDynamicUpdates()
    {
        return hasNonDynamicUpdates;
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append((new StringBuilder()).append(getSource()).append(" (").append(updateSet.size()).append(" updateSet)").toString());
        for(Iterator iterator = updateSet.iterator(); iterator.hasNext(); sb.append((new StringBuilder()).append("\n  ").append(iterator.next().toString()).toString()));
        return sb.toString();
    }

    private void addUpdate(ConfigPropertyUpdate propertyupdate)
    {
        if(updateSet == Collections.EMPTY_SET)
            updateSet = new LinkedHashSet<ConfigPropertyUpdate>();
        updateSet.add(propertyupdate);
        updateList = null;
    }

    /*public int getBeanDiffID()
    {
        return beanDiffID;
    }*/
    
    /*private class ReadOnlyConfigBeanDiff extends ConfigBeanDiff
    {

        public void recordChange(String propertyName, boolean isDynamic)
        {
            throw new UnsupportedOperationException("Readonly BeanDiff");
        }

        public void recordRemoval(String propertyName, Object removedObj, boolean isDynamic)
        {
            throw new UnsupportedOperationException("Readonly BeanDiff");
        }

        public void recordAddition(String propertyName, Object addedObj, boolean isDynamic)
        {
            throw new UnsupportedOperationException("Readonly BeanDiff");
        }

        public ReadOnlyConfigBeanDiff(ConfigBean sourceBean, ConfigBean proposedBean, int updateID, int beanDiffID)
        {
            super(sourceBean, proposedBean, updateID, beanDiffID);
        }
    }*/
}
