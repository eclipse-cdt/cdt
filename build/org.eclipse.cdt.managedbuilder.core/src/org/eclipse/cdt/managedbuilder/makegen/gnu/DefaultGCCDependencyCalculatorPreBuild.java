/*******************************************************************************
 * Copyright (c) 2006, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class DefaultGCCDependencyCalculatorPreBuild implements
		IManagedDependencyGenerator2 {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGeneratorType#getCalculatorType()
	 */
	@Override
	public int getCalculatorType() {
		return TYPE_PREBUILD_COMMANDS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2#getDependencyFileExtension(org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.core.ITool)
	 */
	@Override
	public String getDependencyFileExtension(IConfiguration buildContext, ITool tool) {
		return IManagedBuilderMakefileGenerator.DEP_EXT;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2#getDependencySourceInfo(org.eclipse.core.runtime.IPath, org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.ITool, org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		return new DefaultGCCDependencyCalculatorPreBuildCommands(source, resource, buildContext, tool, topBuildDirectory);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2#getDependencySourceInfo(org.eclipse.core.runtime.IPath, org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.ITool, org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		return new DefaultGCCDependencyCalculatorPreBuildCommands(source, buildContext, tool, topBuildDirectory);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2#postProcessDependencyFile(org.eclipse.core.runtime.IPath, org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.core.ITool, org.eclipse.core.runtime.IPath)
	 */
	@Override
	public boolean postProcessDependencyFile(IPath dependencyFile, IConfiguration buildContext, ITool tool, IPath topBuildDirectory) {
		// Nothing
		return false;
	}

}

