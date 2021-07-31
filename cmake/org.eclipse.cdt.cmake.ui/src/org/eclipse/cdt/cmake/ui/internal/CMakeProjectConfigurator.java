/*******************************************************************************
 * Copyright (c) 2021 Mat Booth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui.internal;

import java.util.List;

import org.eclipse.cdt.cmake.core.CMakeProjectGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.ProjectImportConfigurator;

/**
 * Smart-import strategy for importing pre-existing CMake projects.
 */
public class CMakeProjectConfigurator extends ProjectImportConfigurator {

	@Override
	protected List<String> getProjectFileNames() {
		return List.of("CMakeLists.txt"); //$NON-NLS-1$
	}

	@Override
	protected IGenerator getGenerator(IProject project) {
		// Don't pass any template to the generator, we are importing an existing project
		CMakeProjectGenerator generator = new CMakeProjectGenerator(null);
		generator.setProjectName(project.getName());
		generator.setLocationURI(project.getLocationURI());
		return generator;
	}
}
