package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.texteditor.MarkerAnnotation;
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
	
	
	/**
	 * Adds the given annotation to this model. Associates the 
	 * annotation with the given position. If requested, all annotation
	 * model listeners are informed about this model change. If the annotation
	 * is already managed by this model nothing happens.
	 *
	 * @param annotation the annotation to add
	 * @param position the associate position
	 * @param fireModelChange indicates whether to notify all model listeners
	 */
	protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged){ 
		if (!fAnnotations.containsKey(annotation)) {
			
			// @@@ This is an unfortunate hack because we cannot override addAnnotationMarker() and if we
			// update a marker position, there's no way to update the annotation
			if(annotation instanceof CMarkerAnnotation) {
				int start = ((CMarkerAnnotation)annotation).getErrorStart();
				if(start != -1 && start != position.getOffset()) {
					position.setOffset(start);
					position.setLength(((CMarkerAnnotation)annotation).getErrorLength());
				}
			}
			fAnnotations.put(annotation, position);
			try {
				addPosition(fDocument, position);
			} catch (Exception e) {
			}

			if (fireModelChanged)
				fireModelChanged();
		}
	}
}
