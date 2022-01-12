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
 * This dependency calculator uses the GCC -MMD -MF -MP -MT options in order to
 * generate .d files as a side effect of compilation.
 * See bugzilla 108715 for the discussion of dependency management that led to
 * the creation of this dependency calculator.  Note also that this technique
 * exhibits the failure modes discussed in comment #5.
 *
 * This dependency calculator uses the class DefaultGCCDependencyCalculator2Commands
 * which implements the per-source command information
 *
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DefaultGCCDependencyCalculator2 implements IManagedDependencyGenerator2 {

	@Override
	public int getCalculatorType() {
		return TYPE_BUILD_COMMANDS;
	}

	@Override
	public String getDependencyFileExtension(IConfiguration buildContext, ITool tool) {
		return IManagedBuilderMakefileGenerator.DEP_EXT;
	}

	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IResource resource, IBuildObject buildContext,
			ITool tool, IPath topBuildDirectory) {
		return new DefaultGCCDependencyCalculator2Commands(source, resource, buildContext, tool, topBuildDirectory);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2#getDependencySourceInfo(org.eclipse.core.runtime.IPath, org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.ITool, org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IBuildObject buildContext, ITool tool,
			IPath topBuildDirectory) {
		return new DefaultGCCDependencyCalculator2Commands(source, buildContext, tool, topBuildDirectory);
	}

	@Override
	public boolean postProcessDependencyFile(IPath dependencyFile, IConfiguration buildContext, ITool tool,
			IPath topBuildDirectory) {
		// Nothing
		return false;
	}

}
