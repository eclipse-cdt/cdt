/**********************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Rational Software - Initial Contribution
 **********************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import org.eclipse.cdt.core.model.ICModelMarker;

public class ExternalSearchAnnotationModel extends
		ResourceMarkerAnnotationModel implements IResourceChangeListener {

	protected IWorkspace fWorkspace;
	protected IStorage fStorage;
	protected boolean fChangesApplied;

	/**
	 * @param resource
	 */
	public ExternalSearchAnnotationModel(IResource resource, IStorage storage) {
		super(resource);
		this.fWorkspace = resource.getWorkspace();
		this.fStorage = storage;
	}

	protected IMarker[] retrieveMarkers() throws CoreException {
		IMarker[] markers = null;
		if (getResource() != null) {
			markers = getResource().findMarkers(IMarker.MARKER, true,
					IResource.DEPTH_ZERO);
		}
		return markers;
	}

	protected boolean isAcceptable(IMarker marker) {
		boolean acceptable = false;
		String externalFileName = marker.getAttribute(
				ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION, null);
		if (externalFileName != null) { // Only accept markers with external
										// paths set
			IPath externalPath = new Path(externalFileName);
			IPath storagePath = fStorage.getFullPath();
			acceptable = externalPath.equals(storagePath); // Only accept
															// markers for this
															// annotation
															// model's external
															// editor
		}
		return acceptable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {

	}

}
