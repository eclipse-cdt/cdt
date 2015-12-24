/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.IPathEntryContainerPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ide.IDE;

/**
 * @deprecated as of CDT 4.0. This class was used for Path Container Wizard
 * for 3.X style projects.
 */
@Deprecated
public class ProjectContainerDescriptor implements IContainerDescriptor {
	private int[] fFilterType;
	
	public ProjectContainerDescriptor(int[] filterType) {
		fFilterType = filterType;
	}

	@Override
	public IPathEntryContainerPage createPage() throws CoreException {
		return new ProjectContainerPage(fFilterType);
	}

	@Override
	public String getName() {
		return CPathEntryMessages.ProjectContainer_label; 
	}
	
	@Override
	public Image getImage() {
		return CUIPlugin.getDefault().getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);

	}

	@Override
	public boolean canEdit(IPathEntry entry) {
		return false;
	}

	
}
