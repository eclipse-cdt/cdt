/**********************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.pdomdepgen;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCalculator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/** 
 * @author Doug Schaefer
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class PDOMDependencyCalculator implements IManagedDependencyCalculator {
	
	private final IPath source;
	private final IResource resource;
	private final IBuildObject buildContext;
	private final ITool tool;
	private final IPath topBuildDirectory;
	private IPath[] dependencies;
	
	public PDOMDependencyCalculator(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		this.source = source; 
		this.resource = resource;
		this.buildContext = buildContext;
		this.tool = tool;
		this.topBuildDirectory = topBuildDirectory;
	}
	
	public IPath[] getAdditionalTargets() {
		return null;
	}

	public IPath[] getDependencies() {
		if (dependencies == null) {
			if (resource != null) {
				ICProject project = CoreModel.getDefault().create(resource.getProject());
				try {
					IIndex index = CCorePlugin.getIndexManager().getIndex(project, IIndexManager.ADD_DEPENDENCIES);
					index.acquireReadLock();
					try {
						IIndexFile[] files = index.getFiles(IndexLocationFactory.getWorkspaceIFL((IFile)resource));
						if (files.length > 0) {
							IIndexInclude[] includes = index.findIncludes(files[0], IIndex.DEPTH_INFINITE);

							List<IPath> list = new ArrayList<IPath>();
							for (IIndexInclude inc : includes) {
								if (inc.isResolved()) {
									list.add(IndexLocationFactory.getAbsolutePath(inc.getIncludesLocation()));
								}
							}
							dependencies = list.toArray(new IPath[list.size()]);
						} else
							dependencies = new IPath[0];
					}
					finally {
						index.releaseReadLock();
					}
				} catch (CoreException e) {
//					Activator.getDefault().getLog().log(e.getStatus());
					dependencies = new IPath[0];
				} catch (InterruptedException e) {
					dependencies = new IPath[0];
				}
			} else
				dependencies = new IPath[0];
		}
		
		return dependencies;
	}

	public IBuildObject getBuildContext() {
		return buildContext;
	}

	public IPath getSource() {
		return source;
	}

	public ITool getTool() {
		return tool;
	}

	public IPath getTopBuildDirectory() {
		return topBuildDirectory;
	}

}
