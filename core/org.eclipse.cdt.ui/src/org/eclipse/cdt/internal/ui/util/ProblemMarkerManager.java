package org.eclipse.cdt.internal.ui.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.widgets.Display;

/**
 * Listens to resource deltas and filters for marker changes of type
 * IMarker.PROBLEM Viewers showing error ticks should register as listener to
 * this type.
 */
public class ProblemMarkerManager implements IResourceChangeListener, IAnnotationModelListener, IAnnotationModelListenerExtension {

	/**
	 * Visitors used to filter the element delta changes
	 */
	private static class ProjectErrorVisitor implements IResourceDeltaVisitor {

		private HashSet fChangedElements;

		public ProjectErrorVisitor(HashSet changedElements) {
			fChangedElements = changedElements;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource res = delta.getResource();
			if (res instanceof IProject && delta.getKind() == IResourceDelta.CHANGED) {
				IProject project = (IProject)res;
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
			if ( (delta.getFlags() & IResourceDelta.MARKERS) != 0) {
				IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
				for (int i = 0; i < markerDeltas.length; i++) {
					if (markerDeltas[i].isSubtypeOf(IMarker.PROBLEM)) {
						int kind = markerDeltas[i].getKind();
						if (kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED)
							return true;
						int severity = markerDeltas[i].getAttribute(IMarker.SEVERITY, -1);
						int newSeverity = markerDeltas[i].getMarker().getAttribute(IMarker.SEVERITY, -1);
						if (newSeverity != severity)
							return true;
					}
				}
			}
			return false;
		}
	}

	private ListenerList fListeners;

	public ProblemMarkerManager() {
		fListeners = new ListenerList(5);
	}

	/*
	 * @see IResourceChangeListener#resourceChanged
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		HashSet changedElements = new HashSet();

		try {
			IResourceDelta delta = event.getDelta();
			if (delta != null)
				delta.accept(new ProjectErrorVisitor(changedElements));
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e.getStatus());
		}

		if (!changedElements.isEmpty()) {
			IResource[] changes = (IResource[])changedElements.toArray(new IResource[changedElements.size()]);
			fireChanges(changes, true);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
	 */
	public void modelChanged(IAnnotationModel model) {
		// no action
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAnnotationModelListenerExtension#modelChanged(AnnotationModelEvent)
	 */
	public void modelChanged(AnnotationModelEvent event) {
		if (event instanceof TranslationUnitAnnotationModelEvent) {
			TranslationUnitAnnotationModelEvent cuEvent = (TranslationUnitAnnotationModelEvent)event;
			if (cuEvent.includesProblemMarkerAnnotationChanges()) {
				//IResource[] changes= new IResource[]
				// {cuEvent.getUnderlyingResource()};
				IResource res = cuEvent.getUnderlyingResource();
				if (res != null) {
					fireChanges(new IResource[]{res}, false);
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
			display.asyncExec(new Runnable() {

				public void run() {
					Object[] listeners = fListeners.getListeners();
					for (int i = 0; i < listeners.length; i++) {
						IProblemChangedListener curr = (IProblemChangedListener)listeners[i];
						curr.problemsChanged(changes, markerChanged);
					}
				}
			});
		}
	}
}

