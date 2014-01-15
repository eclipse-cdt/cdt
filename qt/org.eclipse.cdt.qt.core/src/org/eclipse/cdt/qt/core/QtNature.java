/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (QNX) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

@SuppressWarnings("restriction")
public class QtNature implements IProjectNature {
	private static final String ID = "org.eclipse.cdt.qt.core.qtNature";

	private IProject project;

	public static boolean hasNature(IProject project) {
		try {
			return project.hasNature(ID);
		} catch (CoreException e) {
			QtPlugin.log(e);
			return false;
		}
	}

	public static void addNature(IProject project, IProgressMonitor monitor) throws CoreException {
		if (project.isOpen()) {
			if (hasNature(project))
				return;

			IProjectDescription desc = project.getDescription();
			String[] oldIds = desc.getNatureIds();
			String[] newIds = Arrays.copyOf(oldIds, oldIds.length + 1);
			newIds[oldIds.length] = ID;
			desc.setNatureIds(newIds);
			project.setDescription(desc, monitor);
		}
	}

	@Override
	public void configure() throws CoreException {
		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
		if (cProject == null)
			return;

		IIndex index = CCorePlugin.getIndexManager().getIndex(cProject);
		if (!(index instanceof CIndex))
			return;

		// Don't reindex the project if it already has a Qt linkage.  The index will be updated
		// by the normal triggers.
		for(IIndexFragment fragment : ((CIndex) index).getFragments())
			for(IIndexLinkage linkage : fragment.getLinkages())
				if (linkage.getLinkageID() == ILinkage.QT_LINKAGE_ID)
					return;

		// We need to force the index to be rebuilt the first time the Qt nature is added.   If
		// this doesn't happen then the PDOM could have the current version (so nothing would trigger
		// an update) but no Qt content.
		CCorePlugin.log(IStatus.INFO, "Reindexing " + project.getName() + " because Qt nature has been added");
		CCorePlugin.getIndexManager().reindex(cProject);
	}

	@Override
	public void deconfigure() throws CoreException {
		// This space intentionally left blank.
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}
}
