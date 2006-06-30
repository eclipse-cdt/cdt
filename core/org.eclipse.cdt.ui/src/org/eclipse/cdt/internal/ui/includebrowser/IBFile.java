/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.includebrowser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.ui.util.CoreUtility;

public class IBFile {
	public IPath fLocation;
	public ITranslationUnit fTU= null;

	public IBFile(ITranslationUnit tu) {
		fTU= tu;
		IResource r= fTU.getResource();
		if (r != null) {
			fLocation= r.getLocation();
		}
		else {
			fLocation= fTU.getPath();
		}
	}
	
	public IBFile(ICProject preferredProject, IPath location) throws CModelException {
		fLocation= location;
		IFile[] files= ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(location);
		if (files.length > 0) {
			for (int i = 0; i < files.length && fTU == null; i++) {
				IFile file = files[i];
				fTU= IBConversions.fileToTU(file);
			}
		}
		else {
			CoreModel coreModel = CoreModel.getDefault();
			fTU= coreModel.createTranslationUnitFrom(preferredProject, location);
			if (fTU == null) {
				ICProject[] projects= coreModel.getCModel().getCProjects();
				for (int i = 0; i < projects.length && fTU == null; i++) {
					ICProject project = projects[i];
					if (!preferredProject.equals(project)) {
						fTU= coreModel.createTranslationUnitFrom(project, location);
					}
				}
			}
		}
	}

	public IPath getLocation() {
		return fLocation;
	}

	public boolean equals(Object obj) {
		if (obj instanceof IBFile) {
			IBFile file = (IBFile) obj;
			return (CoreUtility.safeEquals(fLocation, file.fLocation) &&
					CoreUtility.safeEquals(fTU, file.fTU));
		}
		return super.equals(obj);
	}

	public int hashCode() {
		return CoreUtility.safeHashcode(fLocation) + CoreUtility.safeHashcode(fTU);
	}

	public ITranslationUnit getTranslationUnit() {
		return fTU;
	}

	public IFile getResource() {
		if (fTU != null) {
			IResource r= fTU.getResource();
			if (r instanceof IFile) {
				return (IFile) r;
			}
		}
		return null;
	}
}