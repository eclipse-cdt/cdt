/*
 * Created on Apr 27, 2004
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.wizards.ICPathContainerPage;
import org.eclipse.core.runtime.CoreException;

public class ProjectContainerDescriptor implements IContainerDescriptor {
	private int fFilterType;
	
	public ProjectContainerDescriptor(int filterType) {
		fFilterType = filterType;
	}

	public ICPathContainerPage createPage() throws CoreException {
		return new ProjectContainerPage(fFilterType);
	}

	public String getName() {
		return CPathEntryMessages.getString("ProjectContainer.label"); //$NON-NLS-1$
	}

	public boolean canEdit(IPathEntry entry) {
		return false;
	}

	
}
