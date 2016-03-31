package org.eclipse.cdt.internal.core.build;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class CBuildConfigurationManager implements ICBuildConfigurationManager {

	@Override
	public IBuildConfiguration createBuildConfiguration(ICBuildConfigurationProvider provider,
			IProject project, String configName, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	@Override
	public ICBuildConfiguration getBuildConfiguration(IBuildConfiguration buildConfig) {
		// TODO Auto-generated method stub
		return null;
	}

}
