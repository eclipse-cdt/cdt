/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/** 
 * Info for ICProject.
 */

class CProjectInfo extends OpenableInfo {

	BinaryContainer vBin;
	ArchiveContainer vLib;
	ILibraryReference[] libReferences;
	IIncludeReference[] incReferences;
	ISourceRoot[] sourceRoots;
	IOutputEntry[] outputEntries;

	Object[] nonCResources = null;

	/**
	 */
	public CProjectInfo(CElement element) {
		super(element);
		vBin = null;
		vLib = null;
	}

	synchronized public IBinaryContainer getBinaryContainer() {
		if (vBin == null) {
			vBin = new BinaryContainer((CProject)getElement());
		}
		return vBin;
	}

	synchronized public IArchiveContainer getArchiveContainer() {
		if (vLib == null) {
			vLib = new ArchiveContainer((CProject)getElement());
		}
		return vLib;
	}

	/**
	 * @return
	 */
	public Object[] getNonCResources(IResource res) {
		if (nonCResources != null)
			return nonCResources;

		// determine if src == project
		boolean srcIsProject = false;
		IPathEntry[] entries = null;
		ICProject cproject = getElement().getCProject();
		IPath projectPath = cproject.getProject().getFullPath();
		char[][] exclusionPatterns = null;
		try {
			entries = cproject.getResolvedPathEntries();
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].getEntryKind() == IPathEntry.CDT_SOURCE) {
					ISourceEntry entry = (ISourceEntry)entries[i];
					if (projectPath.equals(entry.getPath())) {
						srcIsProject = true;
						exclusionPatterns = entry.fullExclusionPatternChars();
						break;
					}
				}
			}
		} catch (CModelException e) {
			// ignore
		}

		ArrayList notChildren = new ArrayList();
		try {
			IResource[] resources = null;
			if (res instanceof IContainer) {
				IContainer container = (IContainer)res;
				resources = container.members(false);
			}

			if (resources != null) {
				for (int i = 0; i < resources.length; i++) {
					IResource member = resources[i];
					switch(member.getType()) {
						case IResource.FILE: {
							String filename = member.getName();
							if (srcIsProject) {
								if (CoreModel.isValidTranslationUnitName(cproject.getProject(), filename) 
									&& !CoreModelUtil.isExcluded(member, exclusionPatterns)) {
									continue;
								} else if (!CoreModelUtil.isExcluded(member, exclusionPatterns)) {
									Object o = CModelManager.getDefault().createBinaryFile((IFile)member);
									if (o != null) {
										continue;
									}
								}
							}
							break;
						}
						case IResource.FOLDER: {
							if (srcIsProject && !CoreModelUtil.isExcluded(member, exclusionPatterns)) {
								continue;
							}
						}
					}
					notChildren.add(member);
				}
			}
		} catch (CoreException e) {
			//System.out.println (e);
			//CPlugin.log (e);
			//e.printStackTrace();
		}
		setNonCResources(notChildren.toArray());	
		return nonCResources;
	}

	/**
	 * @param container
	 * @return
	 */
	public void setNonCResources(Object[] resources) {
		nonCResources = resources;
	}

	/*
	 * Reset the source roots and other caches
	 */
	public void resetCaches() {
		if (libReferences != null) {
			for (int i = 0; i < libReferences.length; i++) {
				try {
					((CElement)libReferences[i]).close();
				} catch (CModelException e) {
					//
				}
			}
		}
		if (incReferences != null) {
			for (int i = 0; i < incReferences.length; i++) {
				try {
					((CElement)incReferences[i]).close();
				} catch (CModelException e) {
					//
				}
			}
		}
		sourceRoots = null;
		outputEntries = null;
		setNonCResources(null);
	}

}
