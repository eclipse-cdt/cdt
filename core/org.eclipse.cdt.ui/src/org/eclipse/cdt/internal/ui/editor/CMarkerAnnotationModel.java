package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;


public class CMarkerAnnotationModel extends ResourceMarkerAnnotationModel {

	/**
	 * Constructor for CMarkerAnnotationModel
	 */
	public CMarkerAnnotationModel(IResource resource) {
		super(resource);
	}

	/**
	 * @see AbstractMarkerAnnotationModel#createMarkerAnnotation(IMarker)
	 */
	protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
		String markerType = MarkerUtilities.getMarkerType(marker);
		return new CMarkerAnnotation(marker, fDocument);
	}
	/**
	 * @see AbstractMarkerAnnotationModel#modifyMarkerAnnotation(IMarker)
	 */
	protected void modifyMarkerAnnotation(IMarker marker) {
		MarkerAnnotation a= getMarkerAnnotation(marker);
		if (a == null) {
			// It might not have been good enough before, but now it 
			// is, try adding this marker into the model again.
			addMarkerAnnotation(marker);
		}
		super.modifyMarkerAnnotation(marker);
	}
	
}
