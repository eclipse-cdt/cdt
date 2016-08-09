/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.CBuilder;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tools.templates.freemarker.FMProjectGenerator;
import org.eclipse.tools.templates.freemarker.SourceRoot;
import org.osgi.framework.Bundle;

public class CMakeProjectGenerator extends FMProjectGenerator {

	public CMakeProjectGenerator(String manifestFile) {
		super(manifestFile);
	}

	@Override
	protected void initProjectDescription(IProjectDescription description) {
		description
				.setNatureIds(
						new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, CMakeNature.ID });
		ICommand command = description.newCommand();
		CBuilder.setupBuilder(command);
		description.setBuildSpec(new ICommand[] { command });
	}

	@Override
	public Bundle getSourceBundle() {
		return Activator.getPlugin().getBundle();
	}

	@Override
	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {
		super.generate(model, monitor);

		// Create the source folders
		IProject project = getProject();
		List<IPathEntry> entries = new ArrayList<>();
		for (SourceRoot srcRoot : getManifest().getSrcRoots()) {
			IFolder sourceFolder = project.getFolder(srcRoot.getDir());
			if (!sourceFolder.exists()) {
				sourceFolder.create(true, true, monitor);
			}

			entries.add(CoreModel.newSourceEntry(sourceFolder.getFullPath()));
		}
		CoreModel.getDefault().create(project).setRawPathEntries(entries.toArray(new IPathEntry[entries.size()]),
				monitor);
	}

}
