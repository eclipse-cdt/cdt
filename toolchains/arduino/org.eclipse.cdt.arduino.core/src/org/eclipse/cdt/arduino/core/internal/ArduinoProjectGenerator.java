/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal;

import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.CBuilder;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tools.templates.freemarker.FMProjectGenerator;
import org.osgi.framework.Bundle;

public class ArduinoProjectGenerator extends FMProjectGenerator {

	public ArduinoProjectGenerator(String manifestFile) {
		super(manifestFile);
	}

	@Override
	protected void initProjectDescription(IProjectDescription description) {
		description.setNatureIds(
				new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, ArduinoProjectNature.ID });
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
		IProject project = getProject();
		CoreModel.getDefault().create(project)
				.setRawPathEntries(new IPathEntry[] { CoreModel.newSourceEntry(project.getFullPath()) }, monitor);
	}

}
