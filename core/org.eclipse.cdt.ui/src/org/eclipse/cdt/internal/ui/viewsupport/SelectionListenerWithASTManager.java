/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ISelectionValidator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;

/**
 * Infrastructure to share an AST for editor post selection listeners.
 */
public class SelectionListenerWithASTManager {
	private static SelectionListenerWithASTManager fgDefault;
	
	/**
	 * @return Returns the default manager instance.
	 */
	public static SelectionListenerWithASTManager getDefault() {
		if (fgDefault == null) {
			fgDefault= new SelectionListenerWithASTManager();
		}
		return fgDefault;
	}
	
	private final static class PartListenerGroup {
		private ITextEditor fPart;
		private ISelectionListener fPostSelectionListener;
		private ISelectionChangedListener fSelectionListener;
		private Job fCurrentJob;
		private ListenerList fAstListeners;
		/** Rule to make sure only one job is running at a time */
		private final ILock fJobLock= Job.getJobManager().newLock();
		private ISelectionValidator fValidator;
		
		public PartListenerGroup(ITextEditor editorPart) {
			fPart= editorPart;
			fCurrentJob= null;
			fAstListeners= new ListenerList(ListenerList.IDENTITY);
			
			fSelectionListener= new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					ISelection selection= event.getSelection();
					if (selection instanceof ITextSelection) {
						fireSelectionChanged((ITextSelection) selection);
					}
				}
			};
			
			fPostSelectionListener= new ISelectionListener() {
				@Override
				public void selectionChanged(IWorkbenchPart part, ISelection selection) {
					if (part == fPart && selection instanceof ITextSelection)
						firePostSelectionChanged((ITextSelection) selection);
				}
			};
		}

		public boolean isEmpty() {
			return fAstListeners.isEmpty();
		}

		public void install(ISelectionListenerWithAST listener) {
			if (isEmpty()) {
				fPart.getEditorSite().getPage().addPostSelectionListener(fPostSelectionListener);
				ISelectionProvider selectionProvider= fPart.getSelectionProvider();
				if (selectionProvider != null) {
					selectionProvider.addSelectionChangedListener(fSelectionListener);
					if (selectionProvider instanceof ISelectionValidator) {
						fValidator= (ISelectionValidator) selectionProvider;
					}
				}
			}
			fAstListeners.add(listener);
		}
		
		public void uninstall(ISelectionListenerWithAST listener) {
			fAstListeners.remove(listener);
			if (isEmpty()) {
				fPart.getEditorSite().getPage().removePostSelectionListener(fPostSelectionListener);
				ISelectionProvider selectionProvider= fPart.getSelectionProvider();
				if (selectionProvider != null) {
					selectionProvider.removeSelectionChangedListener(fSelectionListener);
				}
				fValidator= null;
			}
		}
		
		public void fireSelectionChanged(final ITextSelection selection) {
			if (fCurrentJob != null) {
				fCurrentJob.cancel();
			}
		}
		
		public void firePostSelectionChanged(final ITextSelection selection) {
			if (fCurrentJob != null) {
				fCurrentJob.cancel();
			}
			
			final IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fPart.getEditorInput());
			if (workingCopy == null)
				return;
			
			fCurrentJob= new Job(Messages.SelectionListenerWithASTManager_jobName) { 
				@Override
				public IStatus run(IProgressMonitor monitor) {
					try {
						// Try to acquire the lock
						while (!monitor.isCanceled() && !fJobLock.acquire(10)) {}
						if (!monitor.isCanceled() && isSelectionValid(selection)) {
							return calculateASTandInform(workingCopy, selection, monitor);
						}
					} catch (InterruptedException e) {
					} finally {
						if (fJobLock.getDepth() != 0)
							fJobLock.release();
					}
					return Status.OK_STATUS;
				}
			};
			fCurrentJob.setPriority(Job.DECORATE);
			fCurrentJob.setSystem(true);
			fCurrentJob.schedule();
		}

		/**
		 * Verify that selection is still valid.
		 * 
		 * @param selection
		 * @return <code>true</code> if selection is valid
		 */
		protected boolean isSelectionValid(ITextSelection selection) {
			return fValidator == null || fValidator.isValid(selection);
		}

		protected IStatus calculateASTandInform(final IWorkingCopy workingCopy, final ITextSelection selection, final IProgressMonitor monitor) {
			return ASTProvider.getASTProvider().runOnAST(workingCopy, ASTProvider.WAIT_ACTIVE_ONLY, monitor, new ASTRunnable() {
				@Override
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit astRoot) {
					if (astRoot != null && !monitor.isCanceled() && isSelectionValid(selection)) {
						Object[] listeners;
						synchronized (PartListenerGroup.this) {
							listeners= fAstListeners.getListeners();
						}
						for (int i= 0; i < listeners.length; i++) {
							final Object l = listeners[i];
							try {
								((ISelectionListenerWithAST) l).selectionChanged(fPart, selection, astRoot);
							} catch (RuntimeException e) {
								CUIPlugin.log(e);
								fAstListeners.remove(l);
							} catch (OutOfMemoryError e) {
								CUIPlugin.log(e);
								fAstListeners.remove(l);
							}
						}
						return Status.OK_STATUS;
					}
					return Status.CANCEL_STATUS;
				}
			});
		}
	}
	
	private Map<ITextEditor, PartListenerGroup> fListenerGroups;
	
	private SelectionListenerWithASTManager() {
		fListenerGroups= new HashMap<ITextEditor, PartListenerGroup>();
	}
	
	/**
	 * Registers a selection listener for the given editor part.
	 * @param part The editor part to listen to.
	 * @param listener The listener to register.
	 */
	public void addListener(ITextEditor part, ISelectionListenerWithAST listener) {
		synchronized (this) {
			PartListenerGroup partListener= fListenerGroups.get(part);
			if (partListener == null) {
				partListener= new PartListenerGroup(part);
				fListenerGroups.put(part, partListener);
			}
			partListener.install(listener);
		}
	}

	/**
	 * Unregisters a selection listener.
	 * @param part The editor part the listener was registered.
	 * @param listener The listener to unregister.
	 */
	public void removeListener(ITextEditor part, ISelectionListenerWithAST listener) {
		synchronized (this) {
			PartListenerGroup partListener= fListenerGroups.get(part);
			if (partListener != null) {
				partListener.uninstall(listener);
				if (partListener.isEmpty()) {
					fListenerGroups.remove(part);
				}
			}
		}
	}
}
