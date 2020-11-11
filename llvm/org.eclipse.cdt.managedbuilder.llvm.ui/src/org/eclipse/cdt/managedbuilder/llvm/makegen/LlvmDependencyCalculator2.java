/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Leo Hippelainen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.makegen;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo;
import org.eclipse.cdt.managedbuilder.makegen.gnu.DefaultGCCDependencyCalculator2;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class LlvmDependencyCalculator2 extends DefaultGCCDependencyCalculator2 {

	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IResource resource, IBuildObject buildContext,
			ITool tool, IPath topBuildDirectory) {
		return new LlvmDependencyCalculator2Commands(source, resource, buildContext, tool, topBuildDirectory);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2#getDependencySourceInfo(org.eclipse.core.runtime.IPath,
	 * org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.ITool, org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IBuildObject buildContext, ITool tool,
			IPath topBuildDirectory) {
		return new LlvmDependencyCalculator2Commands(source, buildContext, tool, topBuildDirectory);
	}

}
