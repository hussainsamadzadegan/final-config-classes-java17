package finalconfigclasses.cfg;

public interface EditStrategyFactory {

	public EditStrategy newEditStrategy(ConfigBean sourceBean,
			ConfigBean proposedBean);

}
