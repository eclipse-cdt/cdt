/**********************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.pdomdepgen;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * @author Doug Schaefer
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class PDOMDependencyGenerator implements IManagedDependencyGenerator2 {

	@Override
	public int getCalculatorType() {
		return IManagedDependencyGenerator2.TYPE_CUSTOM;
	}

	@Override
	public String getDependencyFileExtension(IConfiguration buildContext, ITool tool) {
		return ""; //$NON-NLS-1$
	}

	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		return getDependencySourceInfo(source, null, buildContext, tool, topBuildDirectory);
	}

	@Override
	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		if(resource == null && source != null){
			if(!source.isAbsolute())
				source = topBuildDirectory.append(source);

			IFile files[] = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(source);
			if(files.length > 0)
				resource = files[0];
		}

		return new PDOMDependencyCalculator(source, resource, buildContext, tool, topBuildDirectory);
	}

	@Override
	public boolean postProcessDependencyFile(IPath dependencyFile, IConfiguration buildContext, ITool tool, IPath topBuildDirectory) {
		return false;
	}

}
