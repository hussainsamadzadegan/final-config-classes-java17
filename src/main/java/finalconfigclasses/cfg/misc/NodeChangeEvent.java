package finalconfigclasses.cfg.misc;

import java.io.NotSerializableException;
import java.util.EventObject;

import finalconfigclasses.cfg.ConfigBean;

public class NodeChangeEvent extends EventObject {

	private static final long serialVersionUID = -3325409266582203937L;
	
	private String propertyName;
    private Object oldValue;    
    private Object newValue;
    
	public NodeChangeEvent(ConfigBean source, String propertyName,
		     Object oldValue, Object newValue) {
		super(source);
		this.propertyName = propertyName;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}
	
     private void writeObject(java.io.ObjectOutputStream out) 
                                               throws NotSerializableException {
         throw new NotSerializableException("Not serializable.");
     }

     private void readObject(java.io.ObjectInputStream in) 
                                               throws NotSerializableException {
         throw new NotSerializableException("Not serializable.");
     }	
}
