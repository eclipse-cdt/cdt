/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexChangeEvent;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexerStateEvent;
import org.eclipse.cdt.core.index.IIndexerStateListener;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;

/**
 * A single strategy C reconciler.
 * 
 * @since 4.0
 */
public class CReconciler extends MonoReconciler {

	static class SingletonJob extends Job implements ISchedulingRule {
		private Runnable fCode;

		SingletonJob(String name, Runnable code) {
			super(name);
			fCode= code;
			setPriority(Job.SHORT);
			setRule(this);
			setUser(false);
			setSystem(true);
		}

		/*
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (!monitor.isCanceled()) {
				fCode.run();
			}
			return Status.OK_STATUS;
		}

		/*
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		/*
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
		
	}

	/**
	 * Internal part listener for activating the reconciler.
	 */
	private class PartListener implements IPartListener2 {
		/*
		 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
		}
		/*
		 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}
		/*
		 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
		}
		/*
		 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
		}
		/*
		 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == fTextEditor) {
				setEditorActive(false);
			}
		}
		/*
		 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}
		/*
		 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
		}
		/*
		 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == fTextEditor) {
				CReconciler.this.scheduleReconciling();
				setEditorActive(true);
			}
		}
	}

	/**
	 * Internal Shell activation listener for activating the reconciler.
	 */
	private class ActivationListener extends ShellAdapter {

		private Control fControl;

		public ActivationListener(Control control) {
			Assert.isNotNull(control);
			fControl= control;
		}

		/*
		 * @see org.eclipse.swt.events.ShellListener#shellActivated(org.eclipse.swt.events.ShellEvent)
		 */
		@Override
		public void shellActivated(ShellEvent e) {
			if (!fControl.isDisposed() && fControl.isVisible()) {
				if (hasCModelChanged())
					CReconciler.this.scheduleReconciling();
				setEditorActive(true);
			}
		}

		/*
		 * @see org.eclipse.swt.events.ShellListener#shellDeactivated(org.eclipse.swt.events.ShellEvent)
		 */
		@Override
		public void shellDeactivated(ShellEvent e) {
			if (!fControl.isDisposed() && fControl.getShell() == e.getSource()) {
				setEditorActive(false);
			}
		}
	}

	/**
	 * Internal C element changed listener
	 */
	private class ElementChangedListener implements IElementChangedListener {
		/*
		 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
		 */
		@Override
		public void elementChanged(ElementChangedEvent event) {
			if (event.getType() == ElementChangedEvent.POST_CHANGE) {
				if (isRelevantDelta(event.getDelta())) {
					if (!fIsReconciling && isEditorActive() && fInitialProcessDone) {
						CReconciler.this.scheduleReconciling();
					} else {
						setCModelChanged(true);
					}
				}
			}
		}

		private boolean isRelevantDelta(ICElementDelta delta) {
			final int flags = delta.getFlags();
			if ((flags & ICElementDelta.F_CONTENT) != 0) {
				if (!fIsReconciling && isRelevantElement(delta.getElement())) {
					// mark model changed, but don't update immediately
					setCModelChanged(true);
				}
			}
			if ((flags & (
					ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE | 
					ICElementDelta.F_CHANGED_PATHENTRY_MACRO
					)) != 0) {
				if (isRelevantProject(delta.getElement().getCProject())) {
					return true;
				}
			}
			if ((flags & ICElementDelta.F_CHILDREN) != 0) {
				ICElementDelta[] childDeltas= delta.getChangedChildren();
				for (int i = 0; i < childDeltas.length; i++) {
					if (isRelevantDelta(childDeltas[i])) {
						return true;
					}
				}
			}
			return false;
		}
	}

	private class IndexerListener implements IIndexerStateListener, IIndexChangeListener {
		private boolean fIndexChanged;

		/*
		 * @see org.eclipse.cdt.core.index.IIndexerStateListener#indexChanged(org.eclipse.cdt.core.index.IIndexerStateEvent)
		 */
		@Override
		public void indexChanged(IIndexerStateEvent event) {
			if (event.indexerIsIdle()) {
				if (fIndexChanged || hasCModelChanged()) {
					fIndexChanged= false;
					if (!fIsReconciling && isEditorActive() && fInitialProcessDone) {
						CReconciler.this.scheduleReconciling();
					} else {
						setCModelChanged(true);
					}
				}
			}
		}

		/*
		 * @see org.eclipse.cdt.core.index.IIndexChangeListener#indexChanged(org.eclipse.cdt.core.index.IIndexChangeEvent)
		 */
		@Override
		public void indexChanged(IIndexChangeEvent event) {
			if (!fIndexChanged && isRelevantProject(event.getAffectedProject())) {
				fIndexChanged= true;
			}
		}
	}

	/** The reconciler's editor */
	private ITextEditor fTextEditor;
	/** The part listener */
	private IPartListener2 fPartListener;
	/** The shell listener */
	private ShellListener fActivationListener;
	/** The C element changed listener.  */
	private IElementChangedListener fCElementChangedListener;
	/** The indexer listener */
	private IndexerListener fIndexerListener; 
	/** Tells whether the C model might have changed. */
	private volatile boolean fHasCModelChanged= false;
	/** Tells whether this reconciler's editor is active. */
	private volatile boolean fIsEditorActive= true;
	/** Tells whether a reconcile is in progress. */
	private volatile boolean fIsReconciling= false;
	
	private boolean fInitialProcessDone= false;
	private Job fTriggerReconcilerJob;

	/**
	 * Create a reconciler for the given editor and strategy.
	 * 
	 * @param editor the text editor
	 * @param strategy  the C reconciling strategy
	 */
	public CReconciler(ITextEditor editor, CCompositeReconcilingStrategy strategy) {
		super(strategy, false);
		fTextEditor= editor;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconciler#install(org.eclipse.jface.text.ITextViewer)
	 */
	@Override
	public void install(ITextViewer textViewer) {
		super.install(textViewer);
		
		fPartListener= new PartListener();
		IWorkbenchPartSite site= fTextEditor.getSite();
		IWorkbenchWindow window= site.getWorkbenchWindow();
		window.getPartService().addPartListener(fPartListener);

		fActivationListener= new ActivationListener(textViewer.getTextWidget());
		Shell shell= window.getShell();
		shell.addShellListener(fActivationListener);

		fCElementChangedListener= new ElementChangedListener();
		CoreModel.getDefault().addElementChangedListener(fCElementChangedListener);
		
		fIndexerListener= new IndexerListener();
		CCorePlugin.getIndexManager().addIndexerStateListener(fIndexerListener);
		CCorePlugin.getIndexManager().addIndexChangeListener(fIndexerListener);
		
		fTriggerReconcilerJob= new SingletonJob("Trigger Reconciler", new Runnable() { //$NON-NLS-1$
			@Override
			public void run() {
				forceReconciling();
			}});
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconciler#uninstall()
	 */
	@Override
	public void uninstall() {
		fTriggerReconcilerJob.cancel();
		
		IWorkbenchPartSite site= fTextEditor.getSite();
		IWorkbenchWindow window= site.getWorkbenchWindow();
		window.getPartService().removePartListener(fPartListener);
		fPartListener= null;

		Shell shell= window.getShell();
		if (shell != null && !shell.isDisposed())
			shell.removeShellListener(fActivationListener);
		fActivationListener= null;

		CoreModel.getDefault().removeElementChangedListener(fCElementChangedListener);
		fCElementChangedListener= null;

		CCorePlugin.getIndexManager().removeIndexerStateListener(fIndexerListener);
		CCorePlugin.getIndexManager().removeIndexChangeListener(fIndexerListener);
		fIndexerListener= null;
		super.uninstall();
	}

	protected void scheduleReconciling() {
		if (!fInitialProcessDone)
			return;
		if (fTriggerReconcilerJob.cancel()) {
			fTriggerReconcilerJob.schedule(50);
		}
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.AbstractReconciler#forceReconciling()
	 */
	@Override
	protected void forceReconciling() {
		if (!fInitialProcessDone)
			return;
		super.forceReconciling();
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.AbstractReconciler#aboutToBeReconciled()
	 */
	@Override
	protected void aboutToBeReconciled() {
		CCompositeReconcilingStrategy strategy= (CCompositeReconcilingStrategy)getReconcilingStrategy(IDocument.DEFAULT_CONTENT_TYPE);
		strategy.aboutToBeReconciled();
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.MonoReconciler#initialProcess()
	 */
	@Override
	protected void initialProcess() {
		super.initialProcess();
		fInitialProcessDone= true;
		if (!fIsReconciling && isEditorActive() && hasCModelChanged()) {
			CReconciler.this.scheduleReconciling();
		} 
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.MonoReconciler#process(org.eclipse.jface.text.reconciler.DirtyRegion)
	 */
	@Override
	protected void process(DirtyRegion dirtyRegion) {
		fIsReconciling= true;
		setCModelChanged(false);
		super.process(dirtyRegion);
		fIsReconciling= false;
	}

	/**
	 * Tells whether the C Model has changed or not.
	 *
	 * @return <code>true</code> iff the C Model has changed
	 */
	private synchronized boolean hasCModelChanged() {
		return fHasCModelChanged;
	}

	/**
	 * Sets whether the C Model has changed or not.
	 *
	 * @param state <code>true</code> iff the C model has changed
	 */
	private synchronized void setCModelChanged(boolean state) {
		fHasCModelChanged= state;
	}
	
	/**
	 * Tells whether this reconciler's editor is active.
	 *
	 * @return <code>true</code> iff the editor is active
	 */
	private synchronized boolean isEditorActive() {
		return fIsEditorActive;
	}

	/**
	 * Sets whether this reconciler's editor is active.
	 *
	 * @param state <code>true</code> iff the editor is active
	 */
	private synchronized void setEditorActive(boolean active) {
		fIsEditorActive= active;
		if (!active) {
			fTriggerReconcilerJob.cancel();
		}
	}

	public boolean isRelevantElement(ICElement element) {
		if (!fInitialProcessDone) {
			return false;
		}
		if (element instanceof IWorkingCopy) {
			return false;
		}
		if (element instanceof ITranslationUnit) {
			IEditorInput input= fTextEditor.getEditorInput();
			IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();				
			IWorkingCopy copy= manager.getWorkingCopy(input);
			if (copy == null || copy.getOriginalElement().equals(element)) {
				return false;
			}
			return isRelevantProject(copy.getCProject());
		}
		return false;
	}


	private boolean isRelevantProject(ICProject affectedProject) {
		if (affectedProject == null) {
			return false;
		}
		IEditorInput input= fTextEditor.getEditorInput();
		IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();				
		IWorkingCopy copy= manager.getWorkingCopy(input);
		if (copy == null) {
			return false;
		}
		if (copy.getCProject().equals(affectedProject)) {
			return true;
		}
		IProject project= copy.getCProject().getProject();
		if (project == null) {
			return false;
		}
		try {
			IProject[] referencedProjects= project.getReferencedProjects();
			for (int i= 0; i < referencedProjects.length; i++) {
				project= referencedProjects[i];
				if (project.equals(affectedProject.getProject())) {
					return true;
				}
			}
		} catch (CoreException exc) {
		}
		return false;
	}

}
