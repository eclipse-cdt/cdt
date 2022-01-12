/*******************************************************************************
 * Copyright (c) 2006, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.makegen.gnu;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * This dependency calculator uses the GCC -MM -MF -MP -MT options in order to
 * generate .d files as separate step prior to the source compilations.
 *
 * This dependency calculator uses the class DefaultGCCDependencyCalculatorPreBuildCommands
 * which implements the per-source command information
 *
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DefaultGCCDependencyCalculatorPreBuild implements IManagedDependencyGenerator2 {

	@Override
	public int getCalculatorType() {
		return TYPE_PREBUILD_COMMANDS;
	}

	@Override
	public String getDependencyFileExtension(IConfiguration buildContext, ITool tool) {
		return IManagedBuilderMakefileGenerator.DEP_EXT;
	}

	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IResource resource, IBuildObject buildContext,
			ITool tool, IPath topBuildDirectory) {
		return new DefaultGCCDependencyCalculatorPreBuildCommands(source, resource, buildContext, tool,
				topBuildDirectory);
	}

	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IBuildObject buildContext, ITool tool,
			IPath topBuildDirectory) {
		return new DefaultGCCDependencyCalculatorPreBuildCommands(source, buildContext, tool, topBuildDirectory);
	}

	@Override
	public boolean postProcessDependencyFile(IPath dependencyFile, IConfiguration buildContext, ITool tool,
			IPath topBuildDirectory) {
		// Nothing
		return false;
	}

}
