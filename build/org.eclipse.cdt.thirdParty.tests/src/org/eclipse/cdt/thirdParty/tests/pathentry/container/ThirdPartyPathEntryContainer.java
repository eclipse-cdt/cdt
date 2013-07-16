package org.eclipse.cdt.thirdParty.tests.pathentry.container;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainerExtension;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ThirdPartyPathEntryContainer implements IPathEntryContainerExtension {
	public static final String ID = "org.eclipse.cdt.thirdParty.tests.THIRD_PARTY_CONTAINER"; //$NON-NLS-1$
	public static final Path PATH = new Path(ID);
	public static final IContainerEntry fContainerEntry = CoreModel.newContainerEntry(PATH);
	private static final String DESCRIPTION = "Third Party Project Container"; //$NON-NLS-1$

	private IProject fProject;

	public ThirdPartyPathEntryContainer(IProject project) {
		fProject = project;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public IPath getPath() {
		return PATH;
	}

	@Override
	public IPathEntry[] getPathEntries() {
		return getPathEntries(fProject, IPathEntry.CDT_INCLUDE | IPathEntry.CDT_MACRO | IPathEntry.CDT_INCLUDE_FILE | IPathEntry.CDT_MACRO_FILE);
	}

	@Override
	public IPathEntry[] getPathEntries(IPath path, int mask) {
		if(path != null && (mask & (IPathEntry.CDT_INCLUDE | IPathEntry.CDT_MACRO | IPathEntry.CDT_INCLUDE_FILE | IPathEntry.CDT_MACRO_FILE)) != 0) {
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			return getPathEntries(resource, mask);
		}
		return new IPathEntry[0];
	}

	protected IPathEntry[] getPathEntries(IResource resource, int mask) {
		List<IPathEntry> entries = new ArrayList<IPathEntry>();
		if ((mask & IPathEntry.CDT_MACRO) != 0) {
			entries.add(CoreModel.newMacroEntry(resource.getFullPath(),  "CDT_IS_GOOD", "1"));
			entries.add(CoreModel.newMacroEntry(fProject.getFullPath(),  "X_PROJ_LEVEL", "1"));
		}
		if ((mask & IPathEntry.CDT_INCLUDE)!= 0) {
			entries.add(CoreModel.newIncludeEntry(resource.getFullPath(), fProject.getFullPath(), fProject.getLocation()));
			//entries.add(CoreModel.newIncludeEntry(respath, new Path("/tmp/inc"), new Path("/tmp/inc")));
			//entries.add(CoreModel.newIncludeEntry(fProject.getFullPath(), null, new Path("/tmp/inc")));
		}
		return entries.toArray(new IPathEntry[entries.size()]);
	}

	@Override
	public boolean isEmpty(IPath path) {
		if(path != null) {
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			return ! fProject.equals(resource.getProject());
		}
		return true;
	}
}
