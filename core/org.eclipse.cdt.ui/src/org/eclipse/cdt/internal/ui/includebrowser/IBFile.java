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

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.util.CoreUtility;

public class IBFile {
	public IIndexFileLocation fLocation;
	public ITranslationUnit fTU= null;

	public IBFile(ITranslationUnit tu) {
		fTU= tu;
		fLocation= IndexLocationFactory.getIFL(tu);
	}
	
	public IBFile(ICProject preferredProject, IIndexFileLocation location) throws CModelException {
		fLocation= location;
		fTU= CModelUtil.findTranslationUnitForLocation(location, preferredProject);
	}

	public IIndexFileLocation getLocation() {
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
		if (fLocation != null) {
			String fullPath= fLocation.getFullPath();
			if (fullPath != null) {
				IResource file= ResourcesPlugin.getWorkspace().getRoot().findMember(fullPath);
				if (file instanceof IFile) {
					return (IFile) file;
				}
			}
		}
		return null;
	}
}