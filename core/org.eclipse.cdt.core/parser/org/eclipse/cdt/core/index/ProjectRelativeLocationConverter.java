/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * A location converter for converting project resource locations to be project relative. Resources outside of
 * the associated project will be ignored.
 * <br>
 * This location converter is internal-representation-compatible with URIRelativeLocationConverter
 */
 /*
  * Internal representation is project relative path
  */
public class ProjectRelativeLocationConverter implements IIndexLocationConverter {
	protected IWorkspaceRoot root;
	protected String cprojectName;
	
	/**
	 * @param cproject the CDT project to convert relative to
	 */
	public ProjectRelativeLocationConverter(ICProject cproject) {
		this.cprojectName = cproject.getProject().getName();
		this.root = ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexLocationConverter#fromInternalFormat(java.lang.String)
	 */
	public IIndexFileLocation fromInternalFormat(String raw) {
		IResource member= root.getFile(new Path(cprojectName +"/"+ raw)); //$NON-NLS-1$
		return new IndexFileLocation(member.getLocationURI(), member.getFullPath().toString());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexLocationConverter#toInternalFormat(org.eclipse.cdt.core.index.IIndexFileLocation)
	 */
	public String toInternalFormat(IIndexFileLocation location) {
		String fullPath= location.getFullPath();
		if(fullPath!=null) {
			IPath path = new Path(fullPath).removeFirstSegments(1);
			return path.toString();
		}
		return null;
	}
}
