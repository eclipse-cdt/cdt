package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.core.runtime.CoreException;

/**
 * The target type that creates the local target.
 */
public class LocalTargetType implements ILaunchTargetType {

	public static final String ID = Activator.PLUGIN_ID + ".targetType.local";

	@Override
	public void init(ILaunchBarManager manager) throws CoreException {
		// create the local target
		manager.launchTargetAdded(new LocalTarget(this));
	}

	@Override
	public void dispose() {
		// nothing to do
	}

}
