/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.corext.refactoring;

import java.util.Stack;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.cdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.IUndoManager;
import org.eclipse.cdt.internal.corext.refactoring.base.IUndoManagerListener;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Default implementation of IUndoManager.
 */
public class UndoManager implements IUndoManager {

	private class FlushListener implements IElementChangedListener {
		public void elementChanged(ElementChangedEvent event) 
		{
			// If we don't have anything to undo or redo don't examine the tree.
			if (fUndoChanges.isEmpty() && fRedoChanges.isEmpty())
				return;
			
			processDelta(event.getDelta());				
		}

		private boolean processDelta(ICElementDelta delta) 
		{
			int kind= delta.getKind();
			int details= delta.getFlags();
			int type= delta.getElement().getElementType();
			ICElementDelta[] affectedChildren= delta.getAffectedChildren();
			if (affectedChildren == null)
				return true;
				
			switch (type) {
				// Consider containers for class files.
				case ICElement.C_MODEL:
				case ICElement.C_PROJECT:
				case ICElement.C_CCONTAINER:
					// If we did something different than changing a child we flush the the undo / redo stack.
					if (kind != ICElementDelta.CHANGED 
						&& ((details & ICElementDelta.F_CHILDREN) == 0))  {
						flush();
						return false;
					}
					break;
				case ICElement.C_UNIT:
					// if we have changed a primary working copy (e.g created, removed, ...)
					// then we do nothing.
					ITranslationUnit unit= (ITranslationUnit)delta.getElement();
					// If we change a working copy we do nothing
					if (unit.isWorkingCopy()) {
						// Don't examine children of a working copy but keep processing siblings.
						return true;
					} else {
						flush();
						return false;
					}
			}
			for (int i= 0; i < affectedChildren.length; i++) {
				if (!processDelta(affectedChildren[i]))
					return false;
			}	
			return true;
		}	
	}
	
	private class SaveListener implements IResourceChangeListener {
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDeltaVisitor visitor= new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource= delta.getResource();
					if (resource.getType() == IResource.FILE && delta.getKind() == IResourceDelta.CHANGED &&
							(delta.getFlags() & IResourceDelta.CONTENT) != 0) {
						if(CoreModel.isValidTranslationUnitName(resource.getProject(), resource.getName())) {
							ITranslationUnit unit= (ITranslationUnit)CoreModel.getDefault().create((IFile)resource);
							if (unit != null && unit.exists()) {
								flush();
								return false;
							}
						}
					}
					return true;
				}
			};
			try {
				IResourceDelta delta= event.getDelta();
				if (delta != null)
					delta.accept(visitor);
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e.getStatus());
			}
		}
	}

	private Stack fUndoChanges;
	private Stack fRedoChanges;
	private Stack fUndoNames;
	private Stack fRedoNames;
	private ListenerList fListeners;
	private FlushListener fFlushListener;
	private SaveListener fSaveListener;
	
	/**
	 * Creates a new undo manager with an empty undo and redo stack.
	 */
	public UndoManager() {
		flush();
	}
	
	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public void addListener(IUndoManagerListener listener) {
		if (fListeners == null)
			fListeners= new ListenerList();
		fListeners.add(listener);
	}
	
	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public void removeListener(IUndoManagerListener listener) {
		if (fListeners == null)
			return;
		fListeners.remove(listener);
	}
	
	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public void aboutToPerformRefactoring() {
		// Remove the resource change listener since we are changing code.
		if (fFlushListener != null)
			CoreModel.getDefault().removeElementChangedListener(fFlushListener);
		if (fSaveListener != null)
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fSaveListener);
	}
	
	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public void refactoringPerformed(boolean success) {
		if (success) {
			if (fFlushListener != null)
				CoreModel.getDefault().addElementChangedListener(fFlushListener);
			if (fSaveListener != null)
				ResourcesPlugin.getWorkspace().addResourceChangeListener(fSaveListener);
		} else {
			flush();
		}
	}

	/* (non-Javadoc)
	 * @see IUndoManager#shutdown()
	 */
	public void shutdown() {
		if (fFlushListener != null)
			CoreModel.getDefault().removeElementChangedListener(fFlushListener);
		if (fSaveListener != null)
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fSaveListener);
	}
	
	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public void flush() {
		flushUndo();
		flushRedo();
		if (fFlushListener != null)
			CoreModel.getDefault().removeElementChangedListener(fFlushListener);
		if (fSaveListener != null)
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fSaveListener);
		
		fFlushListener= null;
		fSaveListener= null;
	}
	
	private void flushUndo(){
		fUndoChanges= new Stack();
		fUndoNames= new Stack();
		fireUndoStackChanged();
	}
	
	private void flushRedo(){
		fRedoChanges= new Stack();
		fRedoNames= new Stack();
		fireRedoStackChanged();
	}
		
	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public void addUndo(String refactoringName, IChange change){
		Assert.isNotNull(refactoringName, "refactoring"); //$NON-NLS-1$
		Assert.isNotNull(change, "change"); //$NON-NLS-1$
		fUndoNames.push(refactoringName);
		fUndoChanges.push(change);
		flushRedo();
		if (fFlushListener == null) {
			fFlushListener= new FlushListener();
			CoreModel.getDefault().addElementChangedListener(fFlushListener);
		}
		if (fSaveListener == null) {
			fSaveListener= new SaveListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fSaveListener);
		}
		fireUndoStackChanged();
	}
	
	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public RefactoringStatus performUndo(ChangeContext context, IProgressMonitor pm) throws CModelException{
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		RefactoringStatus result= new RefactoringStatus();
		
		if (fUndoChanges.empty())
			return result;
			
		IChange change= (IChange)fUndoChanges.peek();
		
		executeChange(result, context, change, pm);
		
		if (!result.hasError()) {
			fUndoChanges.pop();
			fRedoNames.push(fUndoNames.pop());
			fRedoChanges.push(change.getUndoChange());
			fireUndoStackChanged();
			fireRedoStackChanged();
		}
		return result;	
	}

	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public RefactoringStatus performRedo(ChangeContext context, IProgressMonitor pm) throws CModelException{
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		RefactoringStatus result= new RefactoringStatus();

		if (fRedoChanges.empty())
			return result;
			
		IChange change= (IChange)fRedoChanges.peek();
		
		
		executeChange(result, context, change, pm);
		
		if (!result.hasError()) {
			fRedoChanges.pop();
			fUndoNames.push(fRedoNames.pop());
			fUndoChanges.push(change.getUndoChange());
			fireRedoStackChanged();
			fireUndoStackChanged();
		}
		
		return result;
	}

	private void executeChange(RefactoringStatus status, final ChangeContext context, final IChange change, IProgressMonitor pm) throws CModelException {
		if (fFlushListener != null)
			CoreModel.getDefault().removeElementChangedListener(fFlushListener);
		if (fSaveListener != null)
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fSaveListener);
		try {
			pm.beginTask("", 10); //$NON-NLS-1$
			status.merge(change.aboutToPerform(context, new SubProgressMonitor(pm, 2)));
			if (status.hasError())
				return;
				
			CoreModel.run(
				new IWorkspaceRunnable() {
					public void run(IProgressMonitor innerPM) throws CoreException {
						change.perform(context, innerPM);
					}
				},
				new SubProgressMonitor(pm, 8));
		} catch (CModelException e){
			throw e;
		} catch (CoreException e) {
			throw new CModelException(e);
		} finally {
			change.performed();
			if (fFlushListener != null)
				CoreModel.getDefault().addElementChangedListener(fFlushListener);
			if (fSaveListener != null)
				ResourcesPlugin.getWorkspace().addResourceChangeListener(fSaveListener);
			pm.done();
		}
	}
	
	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public boolean anythingToRedo(){
		return !fRedoChanges.empty();
	}
	
	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public boolean anythingToUndo(){
		return !fUndoChanges.empty();
	}
	
	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public String peekUndoName() {
		if (fUndoNames.size() > 0)
			return (String)fUndoNames.peek();
		return null;	
	}
	
	/* (Non-Javadoc)
	 * Method declared in IUndoManager.
	 */
	public String peekRedoName() {
		if (fRedoNames.size() > 0)
			return (String)fRedoNames.peek();
		return null;	
	}
	
	private void fireUndoStackChanged() {
		if (fListeners == null)
			return;
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			((IUndoManagerListener)listeners[i]).undoStackChanged(this);
		}
	}
	
	private void fireRedoStackChanged() {
		if (fListeners == null)
			return;
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			((IUndoManagerListener)listeners[i]).redoStackChanged(this);
		}
	}	
}
