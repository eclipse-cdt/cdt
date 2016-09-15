/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Rational Software - Initial Contribution
 *     Norbert Ploett (Siemens AG)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import org.eclipse.cdt.core.model.ICModelMarker;

public class ExternalSearchAnnotationModel extends ResourceMarkerAnnotationModel {
	private final IPath fLocation;
	private final int fDepth;
	private final String fLocationAttribute;
	
	public ExternalSearchAnnotationModel(IResource markerResource, IPath location) {
		this(markerResource, location, IResource.DEPTH_ZERO, ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION);
	}

	ExternalSearchAnnotationModel(IResource markerResource, IPath location, int depth) {
		this(markerResource, location, depth, ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION);
	}

	ExternalSearchAnnotationModel(IResource markerResource, IPath location, int depth,
			String locationAttribute) {
		super(markerResource);
		fLocation= location;
		fDepth= depth;
		fLocationAttribute= locationAttribute;
	}

	@Override
	protected IMarker[] retrieveMarkers() throws CoreException {
		IMarker[] markers = null;
		if (getResource() != null) {
			markers = getResource().findMarkers(IMarker.MARKER, true, fDepth);
		}
		return markers;
	}

	@Override
	protected boolean isAcceptable(IMarker marker) {
		boolean acceptable = false;
		String externalFileName = marker.getAttribute(fLocationAttribute, null);
		if (externalFileName != null) { // Only accept markers with external paths set.
			IPath externalPath = new Path(externalFileName);
			// Accept only markers for this annotation model's external editor.
			acceptable = externalPath.equals(fLocation);
		}
		return acceptable;
	}
}
