package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.AbstractLaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public class DefaultLaunchConfigurationProvider extends AbstractLaunchConfigurationProvider implements ILaunchConfigurationProvider {
	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		// We may own it but return false to let it percolate through to the descriptor type.
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configation) throws CoreException {
		return false;
	}
}
