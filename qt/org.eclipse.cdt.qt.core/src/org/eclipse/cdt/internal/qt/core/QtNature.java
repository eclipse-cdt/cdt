/*******************************************************************************
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Doug Schaefer (QNX) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.CBuilder;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

@SuppressWarnings("restriction")
public class QtNature implements IProjectNature {
	public static final String ID = "org.eclipse.cdt.qt.core.qtNature"; //$NON-NLS-1$

	private IProject project;

	public static boolean hasNature(IProject project) {
		try {
			return project.hasNature(ID);
		} catch (CoreException e) {
			Activator.log(e);
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

	public static void setupBuilder(IProjectDescription projDesc) {
		ICommand command = projDesc.newCommand();
		CBuilder.setupBuilder(command);
		projDesc.setBuildSpec(new ICommand[] { command });
	}

	@Override
	public void configure() throws CoreException {
		IProjectDescription projDesc = project.getDescription();
		setupBuilder(projDesc);
		project.setDescription(projDesc, new NullProgressMonitor());
	}

	// TODO no longer needed?
	public void configurex() throws CoreException {
		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
		if (cProject == null)
			return;

		IIndex index = CCorePlugin.getIndexManager().getIndex(cProject);
		if (!(index instanceof CIndex))
			return;

		// Don't reindex the project if it already has a Qt linkage. The index
		// will be updated
		// by the normal triggers.
		for (IIndexFragment fragment : ((CIndex) index).getFragments())
			for (IIndexLinkage linkage : fragment.getLinkages())
				if (linkage.getLinkageID() == ILinkage.QT_LINKAGE_ID)
					return;

		// We need to force the index to be rebuilt the first time the Qt nature
		// is added. If
		// this doesn't happen then the PDOM could have the current version (so
		// nothing would trigger
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
