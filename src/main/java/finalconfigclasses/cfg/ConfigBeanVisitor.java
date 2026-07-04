package finalconfigclasses.cfg;

/**
 * <p>
 * Definition of a <em>Visitor</em> interface for a configuration bean
 * structure.
 * </p>
 * <p>
 * The <code>ConfigBean</code> interface defines a <code>accept()</code>,
 * which simplifies traversal of a complex bean hierarchy. A configuration bean
 * implementation must provide a way of visiting all beans in the current
 * hierarchy. This is a typical application of the GoF <em>Visitor</em>
 * pattern.
 * </p>
 * 
 * @see ConfigBean
 * @author !
 */
public interface ConfigBeanVisitor
{
    /**
     * Visits the specified bean. This method is called before eventually
     * existing children(relations) of this bean are processed.
     *
     * @param bean the bean to be visited
     */
    public void visitBeforeChildren(ConfigBean bean) throws Exception;

    /**
     * Visits the specified bean. This method is called after eventually
     * existing children(relations) of this bean have been processed.
     *
     * @param bean the bean to be visited
     */
    public void visitAfterChildren(ConfigBean bean) throws Exception;

    /**
     * Returns a flag whether the actual visit process should be aborted. This
     * method allows a visitor implementation to state that it does not need any
     * further data. It may be used e.g. by visitors that search for a certain
     * bean in the hierarchy. After that bean was found, there is no need to
     * process the remaining beans, too.
     *
     * @return a flag if the visit process should be stopped
     */
    public boolean terminate();
}

