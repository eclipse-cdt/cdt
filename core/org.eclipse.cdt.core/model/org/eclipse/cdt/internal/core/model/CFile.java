package org.eclipse.cdt.internal.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
//import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICFile;

public class CFile extends CResource implements ICFile {
	
	IPath location;

	public CFile(ICElement parent, IFile file) {
		this(parent, file, file.getLocation(), file.getName());
	}

	public CFile(ICElement parent, IFile file, String name) {
		this(parent, file, file.getLocation(), name);
	}

	public CFile(ICElement parent, IPath location) {
	 	this(parent, ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location),
			location, location.lastSegment());
	}

	public CFile(ICElement parent, IResource res, IPath location, String name) {
		super(parent, res, name, CElement.C_FILE);
		this.location = location;
	}


	public IPath getLocation () {
		return location;
	}

	public void setLocation(IPath location) {
		this.location = location;
	}

	public IFile getFile () {
		try {
			return (IFile)getUnderlyingResource();
		} catch (CModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isBinary() {
		return getCFileInfo().isBinary();
	}

	public boolean isArchive() {
		return getCFileInfo().isArchive();
	}

	public boolean isTranslationUnit() {
		return getCFileInfo().isTranslationUnit();
	}

	protected CFileInfo getCFileInfo() {
		return (CFileInfo)getElementInfo();
	}

	protected CElementInfo createElementInfo () {
		return new CFileInfo(this);
	}
}
