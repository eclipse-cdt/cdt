package org.eclipse.cdt.internal.qt.core.build;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.qt.core.QtPlugin;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class QtScannerInfoProvider implements IScannerInfoProvider {

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		try {
			IProject project = resource.getProject();
			IBuildConfiguration config = project.getActiveBuildConfig();
			QtBuildConfiguration qtConfig = config.getAdapter(QtBuildConfiguration.class);
			return qtConfig.getScannerInfo(resource);
		} catch (CoreException e) {
			QtPlugin.log(e);
			return null;
		}
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

}
