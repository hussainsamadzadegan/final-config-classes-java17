package finalconfigclasses.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import finalconfigclasses.bean.PropertyUpdate.UpdateType;

/**
 * Class which models beanDifference between two java beans.
 */
public class BeanDiff extends BeanUpdateEvent {

	private static final long serialVersionUID = -2210634859111618965L;
	
	private Set<PropertyUpdate> updateSet;
    private ArrayList<PropertyUpdate> updateList;
    /*private int beanDiffID;*/
    
    @SuppressWarnings("unchecked")
	public BeanDiff(Object sourceBean, Object proposedBean/*, int updateID, int beanDiffID*/)
    {
        super(sourceBean, proposedBean/*, updateID*/);        
        updateSet = Collections.EMPTY_SET;
        /*this.beanDiffID = beanDiffID;*/
    }

    public void recordChange(String propertyName)
    {
        addUpdate(new PropertyUpdate(
        		propertyName,
        		UpdateType.CHANGE,
        		null));
    }

    public void recordRemoval(String propertyName, Object removedObj)
    {
        addUpdate(new PropertyUpdate(
        		propertyName,
        		UpdateType.REMOVE,
        		removedObj));
    }

    public void recordAddition(String propertyName, Object addedObj)
    {
        addUpdate(new PropertyUpdate(
        		propertyName,
        		UpdateType.ADD,
        		addedObj));
    }

    public int size()
    {
        return updateSet.size();
    }

    public List<PropertyUpdate> getUpdateList()
    {
        if(updateList == null) {
        	updateList = new ArrayList<PropertyUpdate>();
        	updateList.addAll(updateSet);
        }
        return updateList;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append((new StringBuilder()).append(getSource()).append(" (").append(updateSet.size()).append(" updateSet)").toString());
        for(Iterator iterator = updateSet.iterator(); iterator.hasNext(); sb.append((new StringBuilder()).append("\n  ").append(iterator.next().toString()).toString()));
        return sb.toString();
    }

    private void addUpdate(PropertyUpdate propertyupdate)
    {
        if(updateSet == Collections.EMPTY_SET)
            updateSet = new LinkedHashSet<PropertyUpdate>();
        updateSet.add(propertyupdate);
        updateList = null;
    }

    /*public int getBeanDiffID()
    {
        return beanDiffID;
    }*/
    
    /*BeanDiff getReadOnlyBeanDiff()
    {
        LinkedHashSet linkedhashset = new LinkedHashSet(updateSet);
        ReadOnlyBeanDiff readonlybeandiff = new ReadOnlyBeanDiff(getSourceBean(), getProposedBean(), getUpdateID(), getBeanDiffID());
        readonlybeandiff.updateSet = linkedhashset;
        return readonlybeandiff;
    }
        
    private class ReadOnlyBeanDiff extends BeanDiff
    {

        public void recordChange(String propertyName)
        {
            throw new UnsupportedOperationException("Readonly BeanDiff");
        }

        public void recordRemoval(String propertyName, Object removedObj)
        {
            throw new UnsupportedOperationException("Readonly BeanDiff");
        }

        public void recordAddition(String propertyName, Object addedObj)
        {
            throw new UnsupportedOperationException("Readonly BeanDiff");
        }

        public ReadOnlyBeanDiff(Object sourceBean, Object proposedBean, int updateID, int beanDiffID)
        {
            super(sourceBean, proposedBean, updateID, beanDiffID);
        }
    }*/
}
