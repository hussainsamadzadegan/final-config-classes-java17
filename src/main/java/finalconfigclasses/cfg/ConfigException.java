package finalconfigclasses.cfg;

public class ConfigException extends Exception {

	private static final long serialVersionUID = 2277864617594844351L;

	public ConfigException() {
	}
	
	public ConfigException(String msg) {
		super(msg);
	}
	
	public ConfigException(Throwable cause) {
		super(cause);
	}
	
	public ConfigException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
