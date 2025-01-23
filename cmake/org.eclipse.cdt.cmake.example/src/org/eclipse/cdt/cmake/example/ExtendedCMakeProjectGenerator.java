/*******************************************************************************
 * Copyright (c) 2015, 2025 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.example;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.cdt.cmake.core.CMakeProjectGenerator;
import org.eclipse.core.resources.IProjectDescription;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class ExtendedCMakeProjectGenerator extends CMakeProjectGenerator {

	public ExtendedCMakeProjectGenerator(String manifestFile) {
		super(manifestFile);
	}

	@Override
	protected void initProjectDescription(IProjectDescription description) {
		super.initProjectDescription(description);

		// Add our nature ID so that our extension to CMakeBuildConfiguration can be used
		var natureIds = new ArrayList<>(Arrays.asList(description.getNatureIds()));
		natureIds.remove(CMakeNature.ID);
		natureIds.add(ExtendedCMakeNature.ID);
		description.setNatureIds(natureIds.toArray(String[]::new));
	}

	@Override
	public Bundle getSourceBundle() {
		return FrameworkUtil.getBundle(ExtendedCMakeProjectGenerator.class);
	}

}
