package finalconfigclasses.bean;

/**
 * Indicates that the BeanUpdateListener could not apply the changes on
 * config consumer classes, but this exception would not cause that DiffHelper
 * ignore the rest of updates, The updating process would be continued(DiffHelper
 * may just print the error on console or into log files).
 */
public class BeanUpdateFailedException extends Exception {

	private static final long serialVersionUID = 2235961316297248089L;

	public BeanUpdateFailedException() {
	}

	public BeanUpdateFailedException(String msg) {
		super(msg);
	}

	public BeanUpdateFailedException(String msg, Throwable throwable) {
		super(msg);
		initCause(throwable);
	}
}
