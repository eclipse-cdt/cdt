package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.core.runtime.CoreException;

public class LocalTargetType implements ILaunchTargetType {

	public static final String ID = Activator.PLUGIN_ID + ".targetType.local";

	@Override
	public void init(ILaunchBarManager manager) {
		try {
			manager.launchTargetAdded(new LocalTarget(this));
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
	}
	
	@Override
	public void dispose() {
	}

}
