package org.eclipse.cdt.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.BaseCElementContentProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

public class CElementContentProvider extends BaseCElementContentProvider implements ITreeContentProvider, IElementChangedListener {

	protected StructuredViewer fViewer;
	protected Object fInput;

	/* (non-Cdoc)
	 * Method declared on IContentProvider.
	 */
	public void dispose() {
		super.dispose();
		CoreModel.getDefault().removeElementChangedListener(this);
	}

	/* (non-Cdoc)
	 * Method declared on IContentProvider.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		super.inputChanged(viewer, oldInput, newInput);

		fViewer = (StructuredViewer)viewer;

		if (oldInput == null && newInput != null) {
			if (newInput instanceof ICModel)
				CoreModel.getDefault().addElementChangedListener(this);
		} else if (oldInput != null && newInput == null) {
			CoreModel.getDefault().removeElementChangedListener(this);
		}
		fInput= newInput;
	}

	/**
	 * Creates a new content provider for C elements.
	 */
	public CElementContentProvider() {
	}

	/**
	 * Creates a new content provider for C elements.
	 */
	public CElementContentProvider(boolean provideMembers, boolean provideWorkingCopy) {
		super(provideMembers, provideWorkingCopy);
	}

	/* (non-Cdoc)
	 * Method declared on IElementChangedListener.
	 */
	public void elementChanged(final ElementChangedEvent event) {
		try {
			processDelta(event.getDelta());
		} catch(CModelException e) {
			CUIPlugin.getDefault().log(e);
			e.printStackTrace();
		}
	}

	/**
	 * Processes a delta recursively. When more than two children are affected the
	 * tree is fully refreshed starting at this node. The delta is processed in the
	 * current thread but the viewer updates are posted to the UI thread.
	 */
	protected void processDelta(ICElementDelta delta) throws CModelException {
		int kind= delta.getKind();
		int flags= delta.getFlags();
		ICElement element= delta.getElement();

		//System.out.println("Processing " + element);
		// handle open and closing of a solution or project
		if (((flags & ICElementDelta.F_CLOSED) != 0)
				|| ((flags & ICElementDelta.F_OPENED) != 0)) {
			postRefresh(element);
		}

		if (kind == ICElementDelta.REMOVED) {
			Object parent = internalGetParent(element);
			postRemove(element);
			if (updateContainer(element)) {
				postRefresh(parent);
			}
		}

		if (kind == ICElementDelta.ADDED) {
			Object parent= internalGetParent(element);
			postAdd(parent, element);
			if (updateContainer(element)) {
				postRefresh(parent);
			}
		}

		if (kind == ICElementDelta.CHANGED) {
			if ((flags & ICElementDelta.F_BINARY_PARSER_CHANGED) != 0) {
				// throw the towel and do a full refresh of the affected C project. 
				postRefresh(element.getCProject());
				return;
			} else if (element instanceof ITranslationUnit ||
					element instanceof IBinary || element instanceof IArchive) {
				postRefresh(element);
				return;
			}

		}

		if (delta.getResourceDeltas() != null) {
			IResourceDelta[] rd= delta.getResourceDeltas();
			for (int i= 0; i < rd.length; i++) {
				processResourceDelta(rd[i], element);
			}
		}

		ICElementDelta[] affectedChildren= delta.getAffectedChildren();
		for (int i= 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}

		// Make sure that containers are updated.
		//if (element instanceof ICModel) {
		//	updateContainer((ICModel)element);
		//}
	}

	/*
	 * Process resource deltas
	 */
	private void processResourceDelta(IResourceDelta delta, Object parent) {
		int status= delta.getKind();
		IResource resource= delta.getResource();
		// filter out changes affecting the output folder
		if (resource == null) {
			return;
		}
                        
		// this could be optimized by handling all the added children in the parent
		if ((status & IResourceDelta.REMOVED) != 0) {
//			if (!(parent instanceof ICContainer)) {
//				// refresh one level above to deal with empty package filtering properly
//				postRefresh(internalGetParent(parent));
//			} else {
//				postRemove(resource);
//			}
			postRemove(resource);
		}
		if ((status & IResourceDelta.ADDED) != 0) {
//			if (!(parent instanceof ICContainer)) {
//				// refresh one level above to deal with empty package filtering properly
//				postRefresh(internalGetParent(parent));
//			} else {
//				postAdd(parent, resource);
//			}
			postAdd(parent, resource);
		}
		IResourceDelta[] affectedChildren= delta.getAffectedChildren();

		if (affectedChildren.length > 1) {
			// more than one child changed, refresh from here downwards
			postRefresh(resource);
			return;
		}

		for (int i= 0; i < affectedChildren.length; i++) {
			processResourceDelta(affectedChildren[i], resource);
		}
	}


//	private void updateContainer(ICModel root) {
//		postRunnable(new Runnable() {
//			public void run () {
//				Control ctrl= fViewer.getControl();
//				if (ctrl != null && !ctrl.isDisposed()) {
//					IStructuredSelection s = (IStructuredSelection)fViewer.getSelection();
//					if (s.isEmpty())
//						return;
//					Object element = s.getFirstElement();
//					if (element instanceof ICProject) {
//						updateContainer((ICProject)element);
//					}
//				}
//			}
//		});
//	}

//	protected boolean updateContainer(ICProject cproject) {
//		IParent binContainer = cproject.getBinaryContainer();
//		IParent libContainer = cproject.getArchiveContainer();
//		if (binContainer != null) {
//			postContainerRefresh(binContainer, cproject);
//		}
//		if (libContainer != null) {
//			postContainerRefresh(libContainer, cproject);
//		}
//		return false;
//	}

	private boolean updateContainer(ICElement cfile) {
		IParent container = null;
		ICProject cproject = null;
		if (cfile instanceof IBinary) {
			IBinary bin = (IBinary)cfile;
			if (bin.isExecutable() || bin.isSharedLib()) {
				cproject = bin.getCProject();
				container = cproject.getBinaryContainer();
			}
		} else if (cfile instanceof IArchive) {
			cproject = cfile.getCProject();
			container = cproject.getArchiveContainer();
		}
		if (container != null) {
			postContainerRefresh(container, cproject);
			return true;
		}
		return false;
	}

	private void postContainerRefresh(final IParent container, final ICProject cproject) {
		//System.out.println("UI Container:" + cproject + " " + container);
		postRunnable(new Runnable() {
			public void run () {
				Control ctrl= fViewer.getControl();
				if (ctrl != null && !ctrl.isDisposed()) {
					if (container.hasChildren()) {
						if (fViewer.testFindItem(container) != null) {
							fViewer.refresh(container);
						} else {
							fViewer.refresh(cproject);
						}
					} else {
						fViewer.refresh(cproject);
					}
				}
			}
		});
	}

	private void postRefresh(final Object root) {
		//System.out.println("UI refresh:" + root);
		postRunnable(new Runnable() {
			public void run() {
				// 1GF87WR: ITPUI:ALL - SWTEx + NPE closing a workbench window.
				Control ctrl= fViewer.getControl();
				if (ctrl != null && !ctrl.isDisposed())
					fViewer.refresh(root);
			}
		});
	}

	private void postAdd(final Object parent, final Object element) {
		//System.out.println("UI add:" + parent + " " + element);
		postRunnable(new Runnable() {
			public void run() {
				// 1GF87WR: ITPUI:ALL - SWTEx + NPE closing a workbench window.
				Control ctrl= fViewer.getControl();
				if (ctrl != null && !ctrl.isDisposed())
//					fViewer.add(parent, element);
					fViewer.refresh(parent);
			}
		});
	}

	private void postRemove(final Object element) {
		//System.out.println("UI remove:" + element);
		postRunnable(new Runnable() {
			public void run() {
				// 1GF87WR: ITPUI:ALL - SWTEx + NPE closing a workbench window.
				Control ctrl= fViewer.getControl();
				if (ctrl != null && !ctrl.isDisposed())
//			fViewer.remove(element);
			fViewer.refresh(internalGetParent(element));
			}
		});
	}

	private void postRunnable(final Runnable r) {
		Control ctrl= fViewer.getControl();
		if (ctrl != null && !ctrl.isDisposed())
			ctrl.getDisplay().asyncExec(r);
	}

	/**
	 * The workbench has changed.  Process the delta and issue updates to the viewer,
	 * inside the UI thread.
	 *
	 * @see IResourceChangeListener#resourceChanged
	 */
	//public void resourceChanged(final IResourceChangeEvent event) {
	//	final IResourceDelta delta = event.getDelta();
	//	Control ctrl = viewer.getControl();
	//	if (ctrl != null && !ctrl.isDisposed()) {
	//		ctrl.getDisplay().syncExec(new Runnable() {
	//			public void run() {
	//				processDelta(delta);
	//			}
	//		});
	//	}
	//}


	/**
	 * Returns the implementation of IWorkbenchAdapter for the given
	 * object.  Returns null if the adapter is not defined or the
	 * object is not adaptable.
	 */
	protected ICElement getAdapter(Object o) {
		if (!(o instanceof IAdaptable)) {
			return null;
		}
		return (ICElement)((IAdaptable)o).getAdapter(ICElement.class);
	}

}
