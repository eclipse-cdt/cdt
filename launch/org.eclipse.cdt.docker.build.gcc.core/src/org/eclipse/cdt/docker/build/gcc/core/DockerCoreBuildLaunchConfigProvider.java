package org.eclipse.cdt.docker.build.gcc.core;

import org.eclipse.cdt.debug.internal.core.launch.CoreBuildLocalLaunchConfigProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public class DockerCoreBuildLaunchConfigProvider extends CoreBuildLocalLaunchConfigProvider {
	
	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		return target.getTypeId().equals("org.eclipse.cdt.docker.build.gcc.core.launchTargetType");
	}
}
