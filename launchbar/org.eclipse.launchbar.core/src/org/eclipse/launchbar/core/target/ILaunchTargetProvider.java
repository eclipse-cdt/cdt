package org.eclipse.launchbar.core.target;

/**
 * A launch target provider is responsible for managing the list and status of
 * launch targets. Providers are associated with launch target types in the
 * launchTargetTypes extension point.
 */
public interface ILaunchTargetProvider {

	/**
	 * Called by the launch target manager when it first sees a target of the
	 * type. Or on startup if the provider is enabled. It is expected the
	 * provider will sync the list of targets with it's internal list and alert
	 * the manager of any non-OK statuses.
	 *
	 * @param targetManager
	 */
	void init(ILaunchTargetManager targetManager);

	/**
	 * Fetch the status for the launch target.
	 *
	 * @param target
	 *            the launch target
	 * @return status of the launch target
	 */
	TargetStatus getStatus(ILaunchTarget target);

}
