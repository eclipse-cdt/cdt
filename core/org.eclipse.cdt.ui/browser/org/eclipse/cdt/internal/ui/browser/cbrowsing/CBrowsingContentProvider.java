/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import java.util.Collection;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeCacheChangedListener;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.internal.ui.BaseCElementContentProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public abstract class CBrowsingContentProvider extends BaseCElementContentProvider
	implements ITreeContentProvider, ITypeCacheChangedListener {
	
	protected StructuredViewer fViewer;
	protected Object fInput;
	protected CBrowsingPart fBrowsingPart;
	protected int fReadsInDisplayThread;
	
	public CBrowsingContentProvider(CBrowsingPart browsingPart) {
		fBrowsingPart= browsingPart;
		fViewer= fBrowsingPart.getViewer();
		AllTypesCache.addTypeCacheChangedListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof Collection) {
			// Get a template object from the collection
			Collection col = (Collection) newInput;
			if (!col.isEmpty())
				newInput = col.iterator().next();
			else
				newInput = null;
		}
		fInput = newInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		AllTypesCache.removeTypeCacheChangedListener(this);
	}

	public void typeCacheChanged(IProject project) {
	    if (project.exists() && project.isOpen()) {
	        postAdjustInputAndSetSelection(CoreModel.getDefault().create(project));
	    } else {
	        postAdjustInputAndSetSelection(CoreModel.getDefault().getCModel());
	    }
	}
	
/*	 (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
	 
	public void elementChanged(ElementChangedEvent event) {
		try {
			processDelta(event.getDelta());
		} catch(CModelException e) {
			CUIPlugin.getDefault().log(e.getStatus());
		}
	}

	protected boolean isPathEntryChange(ICElementDelta delta) {
		int flags= delta.getFlags();
		return (delta.getKind() == ICElementDelta.CHANGED && 
				((flags & ICElementDelta.F_BINARY_PARSER_CHANGED) != 0 ||
				(flags & ICElementDelta.F_ADDED_PATHENTRY_LIBRARY) != 0 ||
				(flags & ICElementDelta.F_ADDED_PATHENTRY_SOURCE) != 0 ||
				(flags & ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY) != 0 ||
				(flags & ICElementDelta.F_PATHENTRY_REORDER) != 0 ||
				(flags & ICElementDelta.F_REMOVED_PATHENTRY_SOURCE) != 0 ||
				(flags & ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE) != 0));
	}

	*//**
	 * Processes a delta recursively. When more than two children are affected the
	 * tree is fully refreshed starting at this node. The delta is processed in the
	 * current thread but the viewer updates are posted to the UI thread.
	 *//*
	protected void processDelta(ICElementDelta delta) throws CModelException {
		int kind= delta.getKind();
		int flags= delta.getFlags();
		ICElement element= delta.getElement();

		//System.out.println("Processing " + element);

		// handle open and closing of a solution or project
		if (((flags & ICElementDelta.F_CLOSED) != 0) || ((flags & ICElementDelta.F_OPENED) != 0)) {
			postRefresh(element);
		}

		if (kind == ICElementDelta.REMOVED) {
			postRemove(element);
		}

		if (kind == ICElementDelta.ADDED) {
			Object parent= internalGetParent(element);
			postAdd(parent, element);
		}

		if (kind == ICElementDelta.CHANGED) {
			if (element instanceof ITranslationUnit || element instanceof IBinary || element instanceof IArchive) {
				postRefresh(element);
				return;
			}
		}

		if (isPathEntryChange(delta)) {
			 // throw the towel and do a full refresh of the affected C project. 
			postRefresh(element.getCProject());
		}
		
		ICElementDelta[] affectedChildren= delta.getAffectedChildren();
		for (int i= 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}
	}
*/
	
	private void postAdjustInputAndSetSelection(final Object element) {
		postRunnable(new Runnable() {
			public void run() {
				Control ctrl= fViewer.getControl();
				if (ctrl != null && !ctrl.isDisposed()) {
					ctrl.setRedraw(false);
					
					fBrowsingPart.adjustInputPreservingSelection(element);
					ctrl.setRedraw(true);
				}
			}
		});
	}
	
/*	private void postRefresh(final Object element) {
		//System.out.println("UI refresh:" + root);
		postRunnable(new Runnable() {
			public void run() {
				// 1GF87WR: ITPUI:ALL - SWTEx + NPE closing a workbench window.
				Control ctrl= fViewer.getControl();
				if (ctrl != null && !ctrl.isDisposed()){
					if(element instanceof IWorkingCopy){
						if(fViewer.testFindItem(element) != null){
							fViewer.refresh(element);													
						}else {
							fViewer.refresh(((IWorkingCopy)element).getOriginalElement());
						}
					} else {
						fViewer.refresh(element);						
					}
				}
			}
		});
	}

	private void postAdd(final Object parent, final Object element) {
		//System.out.println("UI add:" + parent + " " + element);
		postRunnable(new Runnable() {
			public void run() {
				// 1GF87WR: ITPUI:ALL - SWTEx + NPE closing a workbench window.
				Control ctrl= fViewer.getControl();
				if (ctrl != null && !ctrl.isDisposed()){
					if(parent instanceof IWorkingCopy){
						if(fViewer.testFindItem(parent) != null){
							fViewer.refresh(parent);													
						}else {
							fViewer.refresh(((IWorkingCopy)parent).getOriginalElement());
						}
					}else {
						fViewer.refresh(parent);						
					}
				}
			}
		});
	}

	private void postRemove(final Object element) {
		//System.out.println("UI remove:" + element);
		postRunnable(new Runnable() {
			public void run() {
				// 1GF87WR: ITPUI:ALL - SWTEx + NPE closing a workbench window.
				Control ctrl= fViewer.getControl();
				if (ctrl != null && !ctrl.isDisposed()) {
					Object parent = internalGetParent(element);
					if(parent instanceof IWorkingCopy){
						if(fViewer.testFindItem(parent) != null){
							fViewer.refresh(parent);													
						}else {
							fViewer.refresh(((IWorkingCopy)parent).getOriginalElement());
						}
					}else {
						fViewer.refresh(parent);						
					}
				}
			}
		});
	}
*/
	private void postRunnable(final Runnable r) {
		Control ctrl= fViewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			ctrl.getDisplay().asyncExec(r); 
		}
	}

	protected void startReadInDisplayThread() {
		if (isDisplayThread())
			fReadsInDisplayThread++;
	}

	protected void finishedReadInDisplayThread() {
		if (isDisplayThread())
			fReadsInDisplayThread--;
	}
	
	private boolean isDisplayThread() {
		Control ctrl= fViewer.getControl();
		if (ctrl == null)
			return false;
		
		Display currentDisplay= Display.getCurrent();
		return currentDisplay != null && currentDisplay.equals(ctrl.getDisplay());
	}
	
}
