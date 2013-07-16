package org.eclipse.cdt.thirdParty.tests.pathentry.container;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.PathEntryContainerInitializer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class ThirdPartyUserDefinedPathEntryContainerInitializer extends PathEntryContainerInitializer {
	public ThirdPartyUserDefinedPathEntryContainerInitializer() {
		super();
	}

	@Override
	public void initialize(IPath containerPath, ICProject project) throws CoreException {
		if(project != null && ThirdPartyUserDefinedPathEntryContainer.hasRequiredNatures(project.getProject(), false)) {  // must not check for project.isOpen(), since this will not be true here
			CoreModel.setPathEntryContainer(new ICProject[]{project}, new ThirdPartyUserDefinedPathEntryContainer(project.getProject()), null);
		}
	}
}
