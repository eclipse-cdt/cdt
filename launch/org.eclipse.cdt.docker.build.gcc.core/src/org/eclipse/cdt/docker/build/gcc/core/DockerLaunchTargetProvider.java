package org.eclipse.cdt.docker.build.gcc.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.core.target.TargetStatus;

public class DockerLaunchTargetProvider implements ILaunchTargetProvider {

	public static final String LAUNCH_TARGET_NAME = "Docker Container";
	public static final String LAUNCH_TARGET_ID = "org.eclipse.cdt.docker.build.gcc.core.launchTargetType";
	
	@Override
	public TargetStatus getStatus(ILaunchTarget arg0) {
		return TargetStatus.OK_STATUS;
	}

	@Override
	public void init(ILaunchTargetManager targetManager) {
		if (targetManager.getLaunchTarget(LAUNCH_TARGET_ID, LAUNCH_TARGET_NAME) == null) {
			ILaunchTarget target = targetManager.addLaunchTarget(LAUNCH_TARGET_ID, LAUNCH_TARGET_NAME);
			ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
			wc.setAttribute("remote", "true");
			wc.setAttribute(ILaunchTarget.ATTR_OS, Platform.getOS());
			wc.setAttribute(ILaunchTarget.ATTR_ARCH, Platform.getOSArch());
			wc.save();
		}
	}

	
}
