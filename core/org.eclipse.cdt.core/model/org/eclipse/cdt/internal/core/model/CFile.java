package org.eclipse.cdt.internal.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public class CFile extends CElement implements ICFile {

	IFile file;
	
	IPath location;

	public CFile(ICElement parent, IFile file){
		//this (parent, file, ICElement.C_FILE);
		this(parent, file, 0);
	}

	public CFile(ICElement parent, IFile file, int type) {
		this(parent, file, file.getLocation(), file.getName(), type);
	}

	public CFile(ICElement parent, IFile file, String name, int type) {
		this(parent, file, file.getLocation(), name, type);
	}

	public CFile(ICElement parent, IPath location, int type) {
	 	this(parent, ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location),
			location, location.lastSegment(), type);
	}

	public CFile(ICElement parent, IFile res, IPath location, String name, int type) {
		super(parent, name, type);
		this.location = location;
		file = res;
	}

	public IPath getLocation () {
		return location;
	}

	public void setLocation(IPath location) {
		this.location = location;
	}

	public IFile getFile () {
		return file;
	}

	protected CFileInfo getCFileInfo() {
		return (CFileInfo)getElementInfo();
	}

	protected CElementInfo createElementInfo () {
		return new CFileInfo(this);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getResource()
	 */
	public IResource getResource() throws CModelException {
		return file;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getUnderlyingResource()
	 */
	public IResource getUnderlyingResource() throws CModelException {
		return file;
	}

}
