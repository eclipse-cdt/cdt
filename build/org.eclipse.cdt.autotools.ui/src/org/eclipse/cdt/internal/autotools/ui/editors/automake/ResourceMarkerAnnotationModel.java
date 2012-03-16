/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * A marker annotation model whose underlying source of markers is
 * a resource in the workspace.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.</p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ResourceMarkerAnnotationModel extends AbstractMarkerAnnotationModel {

	/**
	 * Internal resource change listener.
	 */
	class ResourceChangeListener implements IResourceChangeListener {
		/*
		 * @see IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent e) {
			IResourceDelta delta= e.getDelta();
			if (delta != null && fResource != null) {
				IResourceDelta child= delta.findMember(fResource.getFullPath());
				if (child != null)
					update(child.getMarkerDeltas());
			}
		}
	}

	/** The workspace. */
	private IWorkspace fWorkspace;
	/** The resource. */
	private IResource fResource;
	/** The resource change listener. */
	private IResourceChangeListener fResourceChangeListener= new ResourceChangeListener();


	/**
	 * Creates a marker annotation model with the given resource as the source
	 * of the markers.
	 *
	 * @param resource the resource
	 */
	public ResourceMarkerAnnotationModel(IResource resource) {
		Assert.isNotNull(resource);
		fResource= resource;
		fWorkspace= resource.getWorkspace();
	}

	/*
	 * @see AbstractMarkerAnnotationModel#isAcceptable(IMarker)
	 */
	protected boolean isAcceptable(IMarker marker) {
		return marker != null && fResource.equals(marker.getResource());
	}

	/**
	 * Updates this model to the given marker deltas.
	 *
	 * @param markerDeltas the array of marker deltas
	 */
	protected void update(IMarkerDelta[] markerDeltas) {

		if (markerDeltas.length ==  0)
			return;

		if (markerDeltas.length == 1) {
			IMarkerDelta delta= markerDeltas[0];
			switch (delta.getKind()) {
				case IResourceDelta.ADDED :
					addMarkerAnnotation(delta.getMarker());
					break;
				case IResourceDelta.REMOVED :
					removeMarkerAnnotation(delta.getMarker());
					break;
				case IResourceDelta.CHANGED :
					modifyMarkerAnnotation(delta.getMarker());
					break;
			}
		} else
			batchedUpdate(markerDeltas);

		fireModelChanged();
	}

	/**
	 * Updates this model to the given marker deltas.
	 *
	 * @param markerDeltas the array of marker deltas
	 */
	@SuppressWarnings("unchecked")
	private void batchedUpdate(IMarkerDelta[] markerDeltas) {
		HashSet removedMarkers= new HashSet(markerDeltas.length);
		HashSet modifiedMarkers= new HashSet(markerDeltas.length);

		for (int i= 0; i < markerDeltas.length; i++) {
			IMarkerDelta delta= markerDeltas[i];
			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					addMarkerAnnotation(delta.getMarker());
					break;
				case IResourceDelta.REMOVED:
					removedMarkers.add(delta.getMarker());
					break;
				case IResourceDelta.CHANGED:
					modifiedMarkers.add(delta.getMarker());
					break;
				}
		}

		if (modifiedMarkers.isEmpty() && removedMarkers.isEmpty())
			return;

		Iterator e= getAnnotationIterator(false);
		while (e.hasNext()) {
			Object o= e.next();
			if (o instanceof MarkerAnnotation) {
				MarkerAnnotation a= (MarkerAnnotation)o;
				IMarker marker= a.getMarker();

				if (removedMarkers.remove(marker))
					removeAnnotation(a, false);

				if (modifiedMarkers.remove(marker)) {
					Position p= createPositionFromMarker(marker);
					if (p != null) {
						a.update();
						modifyAnnotationPosition(a, p, false);
					}
				}

				if (modifiedMarkers.isEmpty() && removedMarkers.isEmpty())
					return;

			}
		}

		Iterator iter= modifiedMarkers.iterator();
		while (iter.hasNext())
			addMarkerAnnotation((IMarker)iter.next());
	}

	/*
	 * @see AbstractMarkerAnnotationModel#listenToMarkerChanges(boolean)
	 */
	protected void listenToMarkerChanges(boolean listen) {
		if (listen)
			fWorkspace.addResourceChangeListener(fResourceChangeListener);
		else
			fWorkspace.removeResourceChangeListener(fResourceChangeListener);
	}

	/*
	 * @see AbstractMarkerAnnotationModel#deleteMarkers(IMarker[])
	 */
	protected void deleteMarkers(final IMarker[] markers) throws CoreException {
		fWorkspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i= 0; i < markers.length; ++i) {
					markers[i].delete();
				}
			}
		}, null, IWorkspace.AVOID_UPDATE, null);
	}

	/*
	 * @see AbstractMarkerAnnotationModel#retrieveMarkers()
	 */
	protected IMarker[] retrieveMarkers() throws CoreException {
		return fResource.findMarkers(IMarker.MARKER, true, IResource.DEPTH_ZERO);
	}

	/**
	 * Returns the resource serving as the source of markers for this annotation model.
	 *
	 * @return the resource serving as the source of markers for this annotation model
	 * @since 2.0
	 */
	protected IResource getResource() {
		return fResource;
	}
}
