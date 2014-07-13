package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;

public class LocalTargetType implements ILaunchTargetType {

	public static final String ID = "org.eclipse.cdt.launchbar.core.target.local";
	private LocalTarget localTarget;

	@Override
	public void init(ILaunchBarManager manager) {
		localTarget = new LocalTarget(this);
	}
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ILaunchTarget[] getTargets() {
		return new ILaunchTarget[] { localTarget };
	}

	@Override
	public ILaunchTarget getTarget(String id) {
		if (ID.equals(id))
			return localTarget;
		return null;
	}
	
}
