/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Leo Hippelainen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.makegen;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.gnu.DefaultGCCDependencyCalculator2Commands;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class LlvmDependencyCalculator2Commands extends
		DefaultGCCDependencyCalculator2Commands {


		/**
		 * @param source IPath
		 * @param resource IResource
		 * @param buildContext IBuildObject
		 * @param tool ITool
		 * @param topBuildDirectory IPath
		 */
	public LlvmDependencyCalculator2Commands(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory){
		super(source, resource, buildContext, tool, topBuildDirectory);
	}

	/**
	 * @param source IPath
	 * @param buildContext IBuildObject
	 * @param tool ITool
	 * @param topBuildDirectory IPath
	 */
	public LlvmDependencyCalculator2Commands(IPath source,
			IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		super(source, buildContext, tool, topBuildDirectory);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands#getDependencyCommandOptions()
	 */
	@Override
	public String[] getDependencyCommandOptions() {
		String[] options = new String[2];
		// -MMD
		options[0] = "-MMD";						//$NON-NLS-1$
		// -MP
		options[1] = "-MP";							//$NON-NLS-1$
		// TODO: Check if -MF and/or -MT supported or needed with Clang
		return options;
	}

}
