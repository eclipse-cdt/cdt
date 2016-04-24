/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.internal.CMakeTemplateGenerator;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class CMakeProjectGenerator {

	private final IProject project;

	public CMakeProjectGenerator(IProject project) {
		this.project = project;
	}

	public void generate(IProgressMonitor monitor) throws CoreException {
		// Generate the files
		IFolder sourceFolder = project.getFolder("src"); //$NON-NLS-1$
		if (!sourceFolder.exists()) {
			sourceFolder.create(true, true, monitor);
		}

		CMakeTemplateGenerator templateGen = new CMakeTemplateGenerator();
		Map<String, Object> fmModel = new HashMap<>();
		fmModel.put("projectName", project.getName()); //$NON-NLS-1$

		IFile sourceFile = sourceFolder.getFile("main.cpp"); //$NON-NLS-1$
		templateGen.generateFile(fmModel, "simple/main.cpp", sourceFile, monitor); //$NON-NLS-1$
		sourceFile = project.getFile("CMakeLists.txt"); //$NON-NLS-1$
		templateGen.generateFile(fmModel, "simple/CMakeLists.txt", sourceFile, monitor); //$NON-NLS-1$

		// Set up the project
		IProjectDescription projDesc = project.getDescription();
		String[] oldIds = projDesc.getNatureIds();
		String[] newIds = new String[oldIds.length + 3];
		System.arraycopy(oldIds, 0, newIds, 0, oldIds.length);
		newIds[newIds.length - 3] = CProjectNature.C_NATURE_ID;
		newIds[newIds.length - 2] = CCProjectNature.CC_NATURE_ID;
		newIds[newIds.length - 1] = CMakeNature.ID;
		projDesc.setNatureIds(newIds);

		project.setDescription(projDesc, monitor);

		IPathEntry[] entries = new IPathEntry[] { CoreModel.newOutputEntry(sourceFolder.getFullPath()) };
		CoreModel.getDefault().create(project).setRawPathEntries(entries, monitor);
	}

}
