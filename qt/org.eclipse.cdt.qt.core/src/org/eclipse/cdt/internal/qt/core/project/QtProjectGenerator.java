/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.QtNature;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tools.templates.freemarker.FMProjectGenerator;
import org.eclipse.tools.templates.freemarker.SourceRoot;
import org.osgi.framework.Bundle;

public class QtProjectGenerator extends FMProjectGenerator {

	public QtProjectGenerator(String manifestPath) {
		super(manifestPath);
	}

	@Override
	protected void initProjectDescription(IProjectDescription description) {
		description
				.setNatureIds(new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, QtNature.ID });
		QtNature.setupBuilder(description);
	}

	@Override
	public Bundle getSourceBundle() {
		return Activator.getDefault().getBundle();
	}

	@Override
	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {
		super.generate(model, monitor);

		// Create the source folders
		IProject project = getProject();
		List<IPathEntry> entries = new ArrayList<>();
		List<SourceRoot> srcRoots = getManifest().getSrcRoots();
		if (srcRoots != null && !srcRoots.isEmpty()) {
			for (SourceRoot srcRoot : srcRoots) {
				IFolder sourceFolder = project.getFolder(srcRoot.getDir());
				if (!sourceFolder.exists()) {
					sourceFolder.create(true, true, monitor);
				}

				entries.add(CoreModel.newSourceEntry(sourceFolder.getFullPath()));
			}
		} else {
			entries.add(CoreModel.newSourceEntry(getProject().getFullPath()));
		}

		// build directory as output folder
		entries.add(CoreModel.newOutputEntry(getProject().getFolder("build").getFullPath())); //$NON-NLS-1$

		CoreModel.getDefault().create(project).setRawPathEntries(entries.toArray(new IPathEntry[entries.size()]),
				monitor);
	}

}
