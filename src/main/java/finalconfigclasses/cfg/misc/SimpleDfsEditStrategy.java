package finalconfigclasses.cfg.misc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import finalconfigclasses.bean.BeanUpdateFailedException;
import finalconfigclasses.bean.BeanUpdateListener;
import finalconfigclasses.bean.BeanUpdateRejectedException;
import finalconfigclasses.cfg.ConfigBean;
import finalconfigclasses.cfg.ConfigBeanDiff;
import finalconfigclasses.cfg.ConfigDiffHelper;
import finalconfigclasses.cfg.ConfigException;
import finalconfigclasses.cfg.EditResult;
import finalconfigclasses.cfg.EditStrategy;
import finalconfigclasses.cfg.Constants;
import finalconfigclasses.util.ParameterMap;

/**
 * Simple Depth-First-Search based algorithm. It means this
 * strategy would traverse the bean tree with DFS algorithm.
 * 
 * This algorithm first calls prepareUpdate() of all
 * BeanUpdateListeners and store every BeanUpdateRejectedException.
 * 
 * (A) In case of any reject, this strategy would throw-away all
 * updates and would rollback all listeners.
 * 
 * (B) In case of no reject this strategy would write changes on
 * source beans and would call activateUpdate() on listeners.
 * 
 * In the body of this class we visit ConfigDiffHelpers. You may think
 * it is good idea to add accept() method to ConfigDiffHelper class
 * to accept visitors. But strategy class must specify the traverse algorithm
 * on ConfigDiffHelpers. One strategy may want to use BFS traverse.
 * So we can NOT implement every traversal algorithm in ConfigDiffHelper class.
 * Every strategy must implement traverse algorithm by itself.
 * 
 * see weblogic.descriptor.internal.DescriptorImpl
 * weblogic.management.provider.internal.EditAccessImpl for some implementation.
 */
public final class SimpleDfsEditStrategy extends EditStrategy {

	private ConfigDiffHelper diffHelper;
	private ParameterMap paramMap;
	private EditResult editResult;
    private boolean applyUpdateCalled;
    
	public SimpleDfsEditStrategy(ConfigBean sourceBean, ConfigBean proposedBean) {
		super(sourceBean, proposedBean);
		paramMap = new ParameterMap();
		editResult = new EditResult();
		applyUpdateCalled = false;
	}
	
	public final void computeDiff() {
		if (diffHelper != null)
			throw new IllegalStateException("computeDiff() has already called");
		diffHelper = sourceBean._newDiffHelper();
		diffHelper.computeDiff(proposedBean);
		determineHasNonDynamicUpdates();
	}

	public final boolean hasNonDynamicUpdates() {	
		if (diffHelper == null || diffHelper.getBeanDiff() == null)
			throw new IllegalStateException(
					"hasNonDynamicUpdates() called without first calling computeDiff()");
		determineHasNonDynamicUpdates();
		return editResult.getHasNonDynamicUpdates();
	}
	
	private void determineHasNonDynamicUpdates() {
		if (editResult.getHasNonDynamicUpdates() != null)
			return;

		class MyDiffHelperVisitor implements DiffHelperVisitor {
			boolean result = false;
			
			public boolean terminate() {
				return result;
			}

			public void visit(ConfigDiffHelper cur) {
				ConfigBeanDiff beanDiff = cur.getBeanDiff();
				result |= beanDiff.hasNonDynamicUpdates();
			}			
		}
		
		MyDiffHelperVisitor myVisitor = new MyDiffHelperVisitor();
		dfsVisit(myVisitor);
		editResult.setHasNonDynamicUpdates(myVisitor.result);
	}
	
	public final void applyUpdate() {
        if (applyUpdateCalled)
            throw new IllegalStateException("applyUpdate() has already called");		
		if (diffHelper == null || diffHelper.getBeanDiff() == null)
			throw new IllegalStateException(
					"applyUpdate() called without first calling computeDiff()");
		
		applyUpdateCalled = true;
		prepareUpdate();
		if(editResult.isRejected()) {
			rollbackUpdate();
		} else {
			_applyUpdate();
			activateUpdate();
			boolean save = paramMap.getBoolean(Constants.SAVE_IN_FILE, true);
			boolean threadSafe = paramMap.getBoolean(Constants.MAKE_THREAD_SAFE, false);
			if(save) {
				saveUpdate();
			}
		}
	}

	private void dfsVisit(DiffHelperVisitor visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("Visitor must not be null!");
        }        
		Stack<ConfigDiffHelper> dfsStack = new Stack<ConfigDiffHelper>();
		dfsStack.push(diffHelper);
		while (!dfsStack.isEmpty()) {
			ConfigDiffHelper cur = dfsStack.pop();
			if (!visitor.terminate()) {
				visitor.visit(cur);
			} else {
				break;
			}
			Map<String, List<ConfigDiffHelper>> childMap = cur.getChildren();
			Collection<List<ConfigDiffHelper>> collection = childMap.values();
			for (List<ConfigDiffHelper> list : collection)
				for (ConfigDiffHelper child : list)
					dfsStack.push(child);
		}		
	}
	
	//writing changes on source bean
	private void _applyUpdate() {
		class MyDiffHelperVisitor implements DiffHelperVisitor {
			
			public boolean terminate() {
				return false;
			}

			public void visit(ConfigDiffHelper cur) {
				cur.applyUpdate();
			}			
		}
		dfsVisit(new MyDiffHelperVisitor());
	}
	
	//writing changes of source bean on config file
	private void saveUpdate() {
		class MyDiffHelperVisitor implements DiffHelperVisitor {
			
			public boolean terminate() {
				return false;
			}

			public void visit(ConfigDiffHelper cur) {
				ConfigBeanDiff beanDiff = cur.getBeanDiff();
				ConfigBean source = beanDiff.getSourceBean();
				try {
					System.out.println("saving in zookeeper!");
					source.save();
				} catch (ConfigException e) {
					editResult.addSave(e);
				}
			}			
		}
		dfsVisit(new MyDiffHelperVisitor());		
	}	
	
	//calls listeners for preparation
	private void prepareUpdate() {
		class MyDiffHelperVisitor implements DiffHelperVisitor {
			
			public boolean terminate() {
				return false;
			}

			public void visit(ConfigDiffHelper cur) {
				ConfigBeanDiff beanDiff = cur.getBeanDiff();
				firePrepareUpdate(beanDiff);
			}			
		}
		dfsVisit(new MyDiffHelperVisitor());
	}

	//calls listeners for activation
	private void activateUpdate() {
		class MyDiffHelperVisitor implements DiffHelperVisitor {
			
			public boolean terminate() {
				return false;
			}

			public void visit(ConfigDiffHelper cur) {
				ConfigBeanDiff beanDiff = cur.getBeanDiff();
				fireActivateUpdate(beanDiff);
			}			
		}
		dfsVisit(new MyDiffHelperVisitor());
	}

	//calls listeners for rollback
	private void rollbackUpdate() {
		class MyDiffHelperVisitor implements DiffHelperVisitor {
			
			public boolean terminate() {
				return false;
			}

			public void visit(ConfigDiffHelper cur) {
				ConfigBeanDiff beanDiff = cur.getBeanDiff();
				fireRollbackUpdate(beanDiff);
			}			
		}
		dfsVisit(new MyDiffHelperVisitor());
	}

	private void firePrepareUpdate(ConfigBeanDiff evt) {
		ConfigBean source = evt.getSourceBean();
		BeanUpdateListener[] interested = source.getBeanUpdateListeners();
		for (int i = 0; i < interested.length; i++) {
			try {
				interested[i].prepareUpdate(evt);
			} catch (BeanUpdateRejectedException e) {
				editResult.addReject(e);
			}
		}
	}

	private void fireActivateUpdate(ConfigBeanDiff evt) {
		ConfigBean source = evt.getSourceBean();
		BeanUpdateListener[] interested = source.getBeanUpdateListeners();
		for (int i = 0; i < interested.length; i++) {
			try {
				interested[i].activateUpdate(evt);
			} catch (BeanUpdateFailedException e) {
				editResult.addFail(e);
			}
		}
	}

	private void fireRollbackUpdate(ConfigBeanDiff evt) {
		ConfigBean source = evt.getSourceBean();
		BeanUpdateListener[] interested = source.getBeanUpdateListeners();
		for (int i = 0; i < interested.length; i++)
			interested[i].rollbackUpdate(evt);
	}
	
	private interface DiffHelperVisitor {
		void visit(ConfigDiffHelper cur);
		boolean terminate();
	}
	
	public Map<String, Object> getParameterMap() {
		return paramMap;
	}
	
	public EditResult getEditResult() {
		return editResult;
	}	
}
