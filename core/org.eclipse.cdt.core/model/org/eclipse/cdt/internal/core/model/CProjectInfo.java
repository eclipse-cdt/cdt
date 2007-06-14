/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
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
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
		ICSourceEntry[] entries = null;
		ICProject cproject = getElement().getCProject();
		IProject project = cproject.getProject();
		IPath projectPath = project.getFullPath();
		char[][] exclusionPatterns = null;
		ICProjectDescription des = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		if (des != null) {
			ICConfigurationDescription cfg = des.getDefaultSettingConfiguration();
			if (cfg != null) {
				entries = cfg.getResolvedSourceEntries();
			}
		}

		if (entries != null) {
			for (int i = 0; i < entries.length; i++) {
				ICSourceEntry entry = entries[i];
				if (projectPath.equals(entry.getFullPath())) {
					srcIsProject = true;
					exclusionPatterns = entry.fullExclusionPatternChars();
					break;
				}
			}
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
									if (cproject.isOnOutputEntry(member) && CModelManager.getDefault().createBinaryFile((IFile)member) != null) {
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
