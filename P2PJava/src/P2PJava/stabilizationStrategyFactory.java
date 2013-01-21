package P2PJava;

public class stabilizationStrategyFactory {
	public static stabilizationStrategy createStrategy(boolean succliving, boolean presuccliving, boolean rightness, boolean consistentness) {
		stabilizationStrategy strategy = null;
		if (!succliving) {
			strategy = new stabilizationSuccDeadStrategy();
		} else if (!presuccliving) {
			strategy = new stabilizationPreSuccDeadStrategy();
		} else if (consistentness) {
			strategy = new stabilizationOKStrategy();
		} else if (rightness) {
			strategy = new stabilizationRightStrategy();
		} else {
			strategy = new stabilizationGaucheStrategy();
		}
		return strategy;
	}
}
