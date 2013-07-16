package org.eclipse.cdt.thirdParty.tests.pathentry.container;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainerExtension;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ThirdPartyUserDefinedPathEntryContainer implements IPathEntryContainerExtension {
	private static final String DESCRIPTION = "Third Party Project Container"; //$NON-NLS-1$
	public static final String ID = "org.eclipse.cdt.thirdParty.tests.THIRD_PARTY_USERDEFINED_CONTAINER"; //$NON-NLS-1$
	public static final Path PATH = new Path(ID);

	private IProject fProject; // the project associated with this user-defined container

	public ThirdPartyUserDefinedPathEntryContainer(IProject project) {
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
		return getPathEntries(null, IPathEntry.CDT_MACRO
				| IPathEntry.CDT_INCLUDE | IPathEntry.CDT_INCLUDE_FILE);
	}

	@Override
	public IPathEntry[] getPathEntries(IPath respath, int mask) {
		List<IPathEntry> entries = new ArrayList<IPathEntry>();
		if ((mask & IPathEntry.CDT_MACRO) != 0) {
			entries.add(CoreModel.newMacroEntry(respath,  "CDT_IS_GOOD", "1"));
			entries.add(CoreModel.newMacroEntry(fProject.getFullPath(),  "X_PROJ_LEVEL", "1"));
		}
		if ((mask & IPathEntry.CDT_INCLUDE)!= 0) {
			entries.add(CoreModel.newIncludeEntry(respath, fProject.getLocation(), new Path("inc")));
			entries.add(CoreModel.newIncludeEntry(respath, null, fProject.getLocation().append("inc")));
			//entries.add(CoreModel.newIncludeEntry(respath, new Path("/tmp/inc"), new Path("/tmp/inc")));
			entries.add(CoreModel.newIncludeEntry(fProject.getFullPath(), new Path("/tmp/inc"), null));
		}
		return entries.toArray(new IPathEntry[entries.size()]);
	}

	@Override
	public boolean isEmpty(IPath path) {
		return false;
	}

	public static boolean hasRequiredNatures(IProject project) {
		return hasRequiredNatures(project, true);
	}

	public static boolean hasRequiredNatures(IProject project, boolean checkIsOpen) {
		if (project != null && project.exists()	&& (!checkIsOpen || project.isOpen())) {
			if (CoreModel.hasCNature(project) || CoreModel.hasCCNature(project)) {
				return true;
			}
		}
		return false;
	}

}
