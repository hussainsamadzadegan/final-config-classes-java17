package finalconfigclasses.bean;

/**
 * Indicates that BeanUpdateListener is not agree with changes and DiffHelper should
 * rollback the changes. 
 */
public class BeanUpdateRejectedException extends Exception {

	private static final long serialVersionUID = -7548899841781277804L;

	public BeanUpdateRejectedException() {
	}

	public BeanUpdateRejectedException(String msg) {
		super(msg);
	}

	public BeanUpdateRejectedException(String msg, Throwable throwable) {
		super(msg);
		initCause(throwable);
	}
}
