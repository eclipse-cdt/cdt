/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;

/**
 *  Test build file generator
 */
public class BuildFileGenerator implements IManagedBuilderMakefileGenerator {
	
	private IManagedBuilderMakefileGenerator defGen = new GnuMakefileGenerator();

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#generateDependencies()
	 */
	public void generateDependencies() throws CoreException {
		defGen.generateDependencies();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#generateMakefiles(org.eclipse.core.resources.IResourceDelta)
	 */
	public MultiStatus generateMakefiles(IResourceDelta delta)
			throws CoreException {
		return defGen.generateMakefiles(delta);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#getBuildWorkingDir()
	 */
	public IPath getBuildWorkingDir() {
		IPath current = defGen.getBuildWorkingDir();
		current.append("temp");
		return current;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#getMakefileName()
	 */
	public String getMakefileName() {
		return new String("TestBuildFile.mak");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#initialize(org.eclipse.core.resources.IProject, org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initialize(IProject project, IManagedBuildInfo info,
			IProgressMonitor monitor) {
		defGen.initialize(project, info, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#isGeneratedResource(org.eclipse.core.resources.IResource)
	 */
	public boolean isGeneratedResource(IResource resource) {
		return defGen.isGeneratedResource(resource);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#regenerateDependencies(boolean)
	 */
	public void regenerateDependencies(boolean force) throws CoreException {
		defGen.regenerateDependencies(force);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#regenerateMakefiles()
	 */
	public MultiStatus regenerateMakefiles() throws CoreException {
		return defGen.regenerateMakefiles();
	}

}
