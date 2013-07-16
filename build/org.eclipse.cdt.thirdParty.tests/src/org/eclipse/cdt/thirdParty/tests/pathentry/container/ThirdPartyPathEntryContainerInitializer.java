package org.eclipse.cdt.thirdParty.tests.pathentry.container;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.PathEntryContainerInitializer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class ThirdPartyPathEntryContainerInitializer extends PathEntryContainerInitializer {
	public ThirdPartyPathEntryContainerInitializer() {
		super();
	}

	@Override
	public void initialize(IPath containerPath, ICProject project) throws CoreException {
		if(project != null && project.exists()) { // must not check for project.isOpen(), since this will not be true here
			if(project.getProject().hasNature(CProjectNature.C_NATURE_ID)) {
				CoreModel.setPathEntryContainer(new ICProject[]{project}, new ThirdPartyPathEntryContainer(project.getProject()), null);
			}
		}
	}
}
