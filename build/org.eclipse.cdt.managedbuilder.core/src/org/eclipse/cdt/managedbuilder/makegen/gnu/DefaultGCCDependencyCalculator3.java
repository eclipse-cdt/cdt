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

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * This dependency calculator uses the same dependency management technique as the
 * DefaultGCCDependencyCalculator.  That is:
 *
 *  1.  An echo command creates the dependency file (.d).
 *  2.  A second invocation of the compiler is made in order to append to the dependency file.
 *      The additional options -MM -MG -P -w are added to the command line.
 *  3.  The dependency files are post-processed to add the empty header rules.
 *
 * This dependency calculator uses the class DefaultGCCDependencyCalculator3Commands
 * which implements the per-source command information
 *
 * This is an example dependency calculator that is not used by the CDT GCC tool-chain.
 *
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DefaultGCCDependencyCalculator3 implements IManagedDependencyGenerator2 {

	@Override
	public int getCalculatorType() {
		return TYPE_BUILD_COMMANDS;
	}

	@Override
	public String getDependencyFileExtension(IConfiguration buildContext, ITool tool) {
		return IManagedBuilderMakefileGenerator.DEP_EXT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2#getDependencySourceInfo(org.eclipse.core.runtime.IPath, org.eclipse.core.resources.IResource, org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.ITool, org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IResource resource, IBuildObject buildContext,
			ITool tool, IPath topBuildDirectory) {
		return new DefaultGCCDependencyCalculator3Commands(source, resource, buildContext, tool, topBuildDirectory);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2#getDependencySourceInfo(org.eclipse.core.runtime.IPath, org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.ITool, org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IBuildObject buildContext, ITool tool,
			IPath topBuildDirectory) {
		return new DefaultGCCDependencyCalculator3Commands(source, buildContext, tool, topBuildDirectory);
	}

	@Override
	public boolean postProcessDependencyFile(IPath dependencyFile, IConfiguration buildContext, ITool tool,
			IPath topBuildDirectory) {
		try {
			IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
			IFile makefile;
			IPath makefilePath;
			if (dependencyFile.isAbsolute()) {
				makefilePath = dependencyFile;
			} else {
				makefilePath = topBuildDirectory.append(dependencyFile);
			}
			IPath rootPath = root.getLocation();
			if (rootPath.isPrefixOf(makefilePath)) {
				makefilePath = makefilePath.removeFirstSegments(rootPath.segmentCount());
			}
			makefile = root.getFile(makefilePath);
			IResourceInfo rcInfo = tool.getParentResourceInfo();
			if (rcInfo != null)
				return GnuMakefileGenerator.populateDummyTargets(rcInfo, makefile, false);
			return GnuMakefileGenerator.populateDummyTargets(buildContext, makefile, false);
		} catch (CoreException e) {
		} catch (IOException e) {
		}
		return false;
	}

}
