/**********************************************************************
Copyright (c) 2002, 2004 IBM Rational Software and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
    IBM Rational Software - Initial Contribution
**********************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;


public class ExternalSearchAnnotationModel extends
		AbstractMarkerAnnotationModel implements IResourceChangeListener {

	protected IWorkspace fWorkspace;
	protected IResource fMarkerResource;
	protected boolean fChangesApplied;
	
	/**
	 * @param resource
	 */
	public ExternalSearchAnnotationModel(IResource resource) {
		this.fMarkerResource = resource;
		this.fWorkspace = resource.getWorkspace();
	}

	protected IMarker[] retrieveMarkers() throws CoreException {
		if (fMarkerResource != null)
			return fMarkerResource.findMarkers(IMarker.MARKER, true, IResource.DEPTH_INFINITE);
		return null;
	}

	protected void deleteMarkers(IMarker[] markers) throws CoreException {		
	}
	
	protected void listenToMarkerChanges(boolean listen) {
	}

	protected boolean isAcceptable(IMarker marker) {
		return false;
	}

	public void resourceChanged(IResourceChangeEvent event) {
	}

}
