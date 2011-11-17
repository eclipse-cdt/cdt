/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Chris Recoskie (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;

/**
 * A minimal implementation of ILanguageMappingsChangeEvent.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class LanguageMappingChangeEvent implements ILanguageMappingChangeEvent {

	private int fType;
	private IProject fProject;
	private IFile fFile;
	private IPath fPath;
	private String fFileName;
	private IContentType[] fContentTypes;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguageMappingsChangeEvent#getAffectedContentTypes()
	 */
	@Override
	public IContentType[] getAffectedContentTypes() {
		return fContentTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguageMappingsChangeEvent#getFile()
	 */
	@Override
	public IFile getFile() {
		return fFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguageMappingsChangeEvent#getFilename()
	 */
	@Override
	public String getFilename() {

		// if the filename has been set, use it.  otherwise get the path from
		// either the IFile or the IPath if we have one

		if(fFileName != null)
			return fFileName;
		else {
			if(fFile != null)
			{
				IPath location = fFile.getLocation();
				if(location != null)
					return location.toString();
				else {
					// use the URI if there is one
					URI uri = fFile.getLocationURI();

					if(uri != null)
						return uri.toString();
				}

			}
			else {  // no file, use path
				if(fPath != null)
					return fPath.toString();

			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguageMappingsChangeEvent#getPath()
	 */
	@Override
	public IPath getPath() {

		if(fPath != null)
			return fPath;

		else { // try to get the path from the file if we have one
			if(fFile != null)
			{
				IPath location = fFile.getLocation();
				return location;
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguageMappingsChangeEvent#getProject()
	 */
	@Override
	public IProject getProject() {

		if(fProject != null)
			return fProject;

		else { // try to get the project from the file if we have one

			if(fFile != null)
				return fFile.getProject();

		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguageMappingsChangeEvent#getType()
	 */
	@Override
	public int getType() {
		return fType;
	}

	/**
	 * Sets the associated IContentTypes for this event.  The provided array will be returned
	 * subsequently by getAffectedContentTypes.
	 *
	 * @param affectedContentTypes
	 */
	public void setAffectedContentTypes(IContentType[] affectedContentTypes) {
		fContentTypes = affectedContentTypes;
	}

	/**
	 * Sets the associated IFile for this event.  This file will be returned subsequently
	 * by getFile().
	 *
	 * @param file the IFile to set
	 */
	public void setFile(IFile file) {
		fFile = file;
	}

	/**
	 * Sets the associated String filename for this event.  This filename will be returned subsequently
	 * by getFileName().
	 *
	 * @param fileName the fFileName to set
	 */
	public void setFileName(String fileName) {
		fFileName = fileName;
	}

	/**
	 * Sets the associated IPath for this event.  This path will be returned subsequently
	 * by getPath().
	 *
	 * @param path the IPath to set
	 */
	public void setPath(IPath path) {
		fPath = path;
	}

	/**
	 * Sets the associated project for this event.  This project will be returned subsequently
	 * by getProject().
	 *
	 * @param project the IProject to set
	 */
	public void setProject(IProject project) {
		fProject = project;
	}

	/**
	 * Sets the type of this event.  This type will be returned by getType().
	 *
	 * @param type the type to set
	 * @see ILanguageMappingChangeEvent#TYPE_WORKSPACE
	 * @see ILanguageMappingChangeEvent#TYPE_PROJECT
	 * @see ILanguageMappingChangeEvent#TYPE_FILE
	 */
	public void setType(int type) {
		fType = type;
	}
}
