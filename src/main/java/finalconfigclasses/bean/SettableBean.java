package finalconfigclasses.bean;

/**
 * A bean which implements this interfaces supports the isSet and unSet methods
 * such that a caller can test if a bean has been changed from its default value
 * and also cause the value to be set back to the default.
 * @abstract
 */
public interface SettableBean {

  /**
   * Return true if the given property has been explicitly set in
   * this bean.
   * @param propertyName  property to check
   * @return true if set, false if default
   * @throws IllegalArgumentException if propertyName is not a recognized
   *         property
   */
  public boolean isSet(String propertyName) throws IllegalArgumentException;

  /**
   * Restore the given property to its default value.
   * @param propertyName  property to restore
   * @throws UnsupportedOperationException if called on a runtime
   *         implementation.
   * @throws IllegalArgumentException if propertyName is not a recognized
   *         property
   */
  public void unSet(String propertyName) throws IllegalArgumentException;

}
