/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuilder;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ArduinoProjectGenerator {

	private final IProject project;
	private IFile sourceFile;

	public ArduinoProjectGenerator(IProject project) {
		this.project = project;
	}

	public void generate(IProgressMonitor monitor) throws CoreException {
		// Generate files
		ArduinoTemplateGenerator templateGen = new ArduinoTemplateGenerator();
		Map<String, Object> fmModel = new HashMap<>();
		fmModel.put("projectName", project.getName()); //$NON-NLS-1$

		sourceFile = project.getFile(project.getName() + ".cpp"); //$NON-NLS-1$
		templateGen.generateFile(fmModel, "arduino.cpp", sourceFile, monitor); //$NON-NLS-1$

		// Add natures to project: C, C++, Arduino
		IProjectDescription projDesc = project.getDescription();
		String[] oldIds = projDesc.getNatureIds();
		String[] newIds = new String[oldIds.length + 3];
		System.arraycopy(oldIds, 0, newIds, 0, oldIds.length);
		newIds[newIds.length - 3] = CProjectNature.C_NATURE_ID;
		newIds[newIds.length - 2] = CCProjectNature.CC_NATURE_ID;
		newIds[newIds.length - 1] = ArduinoProjectNature.ID;
		projDesc.setNatureIds(newIds);

		// Add Arduino Builder
		ICommand command = projDesc.newCommand();
		command.setBuilderName(ArduinoBuilder.ID);
		command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, false);
		projDesc.setBuildSpec(new ICommand[] { command });

		project.setDescription(projDesc, monitor);

		IPathEntry[] entries = new IPathEntry[] { CoreModel.newSourceEntry(project.getFullPath()) };
		CoreModel.getDefault().create(project).setRawPathEntries(entries, monitor);
	}

	public IFile getSourceFile() {
		return sourceFile;
	}

}
