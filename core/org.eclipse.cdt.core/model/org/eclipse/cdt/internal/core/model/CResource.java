package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import org.eclipse.cdt.core.model.*;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.CModelException;

public abstract class CResource extends Parent implements ICResource {
	
	public CResource (ICElement parent, IPath path, int type) {
		// Check if the file is under the workspace.
		this (parent, ResourcesPlugin.getWorkspace().getRoot().getFileForLocation (path),
			path.lastSegment(), type);
	}

	public CResource (ICElement parent, IResource resource, int type) {
		this (parent, resource, resource.getName(), type);
	}
	
	public CResource (ICElement parent, IResource resource, String name, int type) {
		super (parent, resource, name, type);
	}

	public IResource getUnderlyingResource() throws CModelException {
		return resource;
	}

	public IResource getResource() throws CModelException {
		return resource;
	}
	
	protected abstract CElementInfo createElementInfo ();
}
