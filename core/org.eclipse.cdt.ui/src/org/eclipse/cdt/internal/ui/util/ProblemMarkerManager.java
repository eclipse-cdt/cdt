/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import java.util.HashSet;

import org.eclipse.cdt.internal.ui.editor.TranslationUnitAnnotationModelEvent;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.swt.widgets.Display;

/**
 * Listens to resource deltas and filters for marker changes of type
 * IMarker.PROBLEM Viewers showing error ticks should register as listener to
 * this type.
 */
public class ProblemMarkerManager
		implements IResourceChangeListener, IAnnotationModelListener, IAnnotationModelListenerExtension {

	/**
	 * Visitors used to filter the element delta changes
	 */
	private static class ProjectErrorVisitor implements IResourceDeltaVisitor {

		private HashSet<IResource> fChangedElements;

		public ProjectErrorVisitor(HashSet<IResource> changedElements) {
			fChangedElements = changedElements;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource res = delta.getResource();
			if (res instanceof IProject && delta.getKind() == IResourceDelta.CHANGED) {
				IProject project = (IProject) res;
				if (!project.isAccessible()) {
					// only track open C projects
					return false;
				}
			}
			checkInvalidate(delta, res);
			return true;
		}

		private void checkInvalidate(IResourceDelta delta, IResource resource) {
			int kind = delta.getKind();
			if (kind == IResourceDelta.REMOVED || kind == IResourceDelta.ADDED
					|| (kind == IResourceDelta.CHANGED && isErrorDelta(delta))) {
				// invalidate the path and all parent paths
				while (resource.getType() != IResource.ROOT && fChangedElements.add(resource)) {
					resource = resource.getParent();
				}
			}
		}

		private boolean isErrorDelta(IResourceDelta delta) {
			if ((delta.getFlags() & IResourceDelta.MARKERS) != 0) {
				IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
				for (IMarkerDelta markerDelta : markerDeltas) {
					if (markerDelta.isSubtypeOf(IMarker.PROBLEM)) {
						int kind = markerDelta.getKind();
						if (kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED)
							return true;
						int severity = markerDelta.getAttribute(IMarker.SEVERITY, -1);
						int newSeverity = markerDelta.getMarker().getAttribute(IMarker.SEVERITY, -1);
						if (newSeverity != severity)
							return true;
					}
				}
			}
			return false;
		}
	}

	ListenerList<IProblemChangedListener> fListeners;

	public ProblemMarkerManager() {
		fListeners = new ListenerList<>();
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		HashSet<IResource> changedElements = new HashSet<>();

		try {
			IResourceDelta delta = event.getDelta();
			if (delta != null)
				delta.accept(new ProjectErrorVisitor(changedElements));
		} catch (CoreException e) {
			CUIPlugin.log(e.getStatus());
		}

		if (!changedElements.isEmpty()) {
			IResource[] changes = changedElements.toArray(new IResource[changedElements.size()]);
			fireChanges(changes, true);
		}

	}

	@Override
	public void modelChanged(IAnnotationModel model) {
		// no action
	}

	@Override
	public void modelChanged(AnnotationModelEvent event) {
		if (event instanceof TranslationUnitAnnotationModelEvent) {
			TranslationUnitAnnotationModelEvent cuEvent = (TranslationUnitAnnotationModelEvent) event;
			if (cuEvent.includesProblemMarkerAnnotationChanges()) {
				//IResource[] changes= new IResource[]
				// {cuEvent.getUnderlyingResource()};
				IResource res = cuEvent.getUnderlyingResource();
				if (res != null) {
					fireChanges(new IResource[] { res }, false);
				}
			}
		}
	}

	/**
	 * Adds a listener for problem marker changes.
	 */
	public void addListener(IProblemChangedListener listener) {
		if (fListeners.isEmpty()) {
			CUIPlugin.getWorkspace().addResourceChangeListener(this);
			CUIPlugin.getDefault().getDocumentProvider().addGlobalAnnotationModelListener(this);
		}
		fListeners.add(listener);
	}

	/**
	 * Removes a <code>IProblemChangedListener</code>.
	 */
	public void removeListener(IProblemChangedListener listener) {
		fListeners.remove(listener);
		if (fListeners.isEmpty()) {
			CUIPlugin.getWorkspace().removeResourceChangeListener(this);
			CUIPlugin.getDefault().getDocumentProvider().removeGlobalAnnotationModelListener(this);
		}
	}

	private void fireChanges(final IResource[] changes, final boolean markerChanged) {
		Display display = SWTUtil.getStandardDisplay();
		if (display != null && !display.isDisposed()) {
			display.asyncExec(() -> {
				for (IProblemChangedListener curr : fListeners) {
					curr.problemsChanged(changes, markerChanged);
				}
			});
		}
	}
}
