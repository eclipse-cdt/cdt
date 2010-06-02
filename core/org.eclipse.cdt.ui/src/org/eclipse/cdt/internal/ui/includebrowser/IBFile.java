/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.ui.util.CoreUtility;

public class IBFile {
	final public ITranslationUnit fTU;
	final public IIndexFileLocation fLocation;
	final public String fName;
	
	public IBFile(ITranslationUnit tu) {
		fTU= tu;
		fLocation= IndexLocationFactory.getIFL(tu);
		fName= tu.getElementName();
	}
	
	public IBFile(ICProject preferredProject, IIndexFileLocation location) throws CModelException {
		fLocation= location;
		fTU= CoreModelUtil.findTranslationUnitForLocation(location, preferredProject);
		String name= fLocation.getURI().getPath();
		fName= name.substring(name.lastIndexOf('/')+1);
	}

	public IBFile(String name) {
		fName= name;
		fLocation= null;
		fTU= null;
	}
	
	public IIndexFileLocation getLocation() {
		return fLocation;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IBFile) {
			IBFile file = (IBFile) obj;
			return (CoreUtility.safeEquals(fLocation, file.fLocation) &&
					CoreUtility.safeEquals(fTU, file.fTU));
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return CoreUtility.safeHashcode(fLocation)
			+ 31* (CoreUtility.safeHashcode(fTU) 
			+ 31* CoreUtility.safeHashcode(fName));
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
	
	public String getName() {
		return fName;
	}
}