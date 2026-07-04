package finalconfigclasses.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import finalconfigclasses.bean.BeanUpdateFailedException;
import finalconfigclasses.bean.BeanUpdateRejectedException;

@SuppressWarnings("unchecked")
public class EditResult extends ConfigException {

	private static final long serialVersionUID = -3922917899490058194L;
	
	protected Boolean hasNonDynamicUpdates;
	protected List<BeanUpdateRejectedException> rejectedList
		= Collections.EMPTY_LIST;
	protected List<BeanUpdateFailedException> failedList
		= Collections.EMPTY_LIST;
	protected List<ConfigException> saveList
		= Collections.EMPTY_LIST;
	
	public EditResult() {
	}
	
	public EditResult(String msg, Throwable cause) {
		super(msg, cause);
	}

	public EditResult(String msg) {
		super(msg);
	}

	public EditResult(Throwable cause) {
		super(cause);
	}

	public void setHasNonDynamicUpdates(Boolean hasNonDynamicUpdates) {
		this.hasNonDynamicUpdates = hasNonDynamicUpdates;
	}

	public void addReject(BeanUpdateRejectedException exp) {
		if(rejectedList == Collections.EMPTY_LIST)
			rejectedList = new ArrayList<BeanUpdateRejectedException>();
		rejectedList.add(exp);
	}
	
	public void addFail(BeanUpdateFailedException exp) {
		if(failedList == Collections.EMPTY_LIST)
			failedList = new ArrayList<BeanUpdateFailedException>();
		failedList.add(exp);
	}
	
	public void addSave(ConfigException exp) {
		if(saveList == Collections.EMPTY_LIST)
			saveList = new ArrayList<ConfigException>();
		saveList.add(exp);
	}
	
	public Boolean getHasNonDynamicUpdates() {
		return hasNonDynamicUpdates;
	}

    public boolean isSuccessful() {
        return !canThrow();
    }
    
	public boolean isRejected() {
		return rejectedList.size() > 0;
	}
	
	public boolean isFailed() {
		return failedList.size() > 0;
	}
	
	public boolean isSaveProblem() {
		return saveList.size() > 0;
	}	
	
	public boolean canThrow() {
		return isRejected() || isFailed() || isSaveProblem();
	}
	
	public BeanUpdateRejectedException[] getRejects() {
		return rejectedList.toArray(new BeanUpdateRejectedException[0]);
	}
	
	public BeanUpdateFailedException[] getFails() {
		return failedList.toArray(new BeanUpdateFailedException[0]);
	}
	
	public ConfigException[] getSaveProblems() {
		return saveList.toArray(new ConfigException[0]);
	}	
}
