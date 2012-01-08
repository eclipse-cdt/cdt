/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ASTCache;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

/**
 * Provides a shared AST for clients. The shared AST is
 * the AST of the active CEditor's input element.
 * 
 * @since 4.0
 */
public final class ASTProvider {
	/**
	 * Wait flag.
	 */
	public static final class WAIT_FLAG {
		String fName;

		private WAIT_FLAG(String name) {
			fName= name;
		}

		/*
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return fName;
		}
	}

	/**
	 * Wait flag indicating that a client requesting an AST
	 * wants to wait until an AST is ready. If the translation unit is not open no AST will
	 * be provided.
	 * <p>
	 * If not yet cached and if the translation unit is open, an AST will be created by 
	 * this AST provider.
	 * </p>
	 */
	public static final WAIT_FLAG WAIT_IF_OPEN= new WAIT_FLAG("wait if open"); //$NON-NLS-1$

	/**
	 * Wait flag indicating that a client requesting an AST
	 * only wants to wait for the shared AST of the active editor. 
	 * If the translation unit is not open no AST will be provided.
	 * <p>
	 * No AST will be created by the AST provider.
	 * </p>
	 */
	public static final WAIT_FLAG WAIT_ACTIVE_ONLY= new WAIT_FLAG("wait active only"); //$NON-NLS-1$

	/**
	 * Wait flag indicating that a client requesting an AST
	 * only wants the already available shared AST.
	 * <p>
	 * No AST will be created by the AST provider.
	 * </p>
	 */
	public static final WAIT_FLAG WAIT_NO= new WAIT_FLAG("don't wait"); //$NON-NLS-1$

	/** Full parse mode (no PDOM) */
	public static int PARSE_MODE_FULL= 0;
	/** Fast parse mode (use PDOM) */
	public static int PARSE_MODE_FAST= ITranslationUnit.AST_SKIP_INDEXED_HEADERS;

	/**
	 * Internal activation listener.
	 */
	private class ActivationListener implements IPartListener2, IWindowListener {

		/*
		 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partActivated(IWorkbenchPartReference ref) {
			if (isCEditor(ref) && !isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partBroughtToTop(IWorkbenchPartReference ref) {
			if (isCEditor(ref) && !isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partClosed(IWorkbenchPartReference ref) {
			if (isActiveEditor(ref)) {
				activeEditorChanged(null);
			}
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partDeactivated(IWorkbenchPartReference ref) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partOpened(IWorkbenchPartReference ref) {
			if (isCEditor(ref) && !isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partHidden(IWorkbenchPartReference ref) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partVisible(IWorkbenchPartReference ref) {
			if (isCEditor(ref) && !isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partInputChanged(IWorkbenchPartReference ref) {
			if (isCEditor(ref) && isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
		public void windowActivated(IWorkbenchWindow window) {
			IWorkbenchPartReference ref= window.getPartService().getActivePartReference();
			if (isCEditor(ref) && !isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
		public void windowDeactivated(IWorkbenchWindow window) {
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
		public void windowClosed(IWorkbenchWindow window) {
			if (fActiveEditor != null && fActiveEditor.getSite() != null && window == fActiveEditor.getSite().getWorkbenchWindow()) {
				activeEditorChanged(null);
			}
			window.getPartService().removePartListener(this);
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
		public void windowOpened(IWorkbenchWindow window) {
			window.getPartService().addPartListener(this);
		}

		private boolean isActiveEditor(IWorkbenchPartReference ref) {
			return ref != null && isActiveEditor(ref.getPart(false));
		}

		private boolean isActiveEditor(IWorkbenchPart part) {
			return part != null && part == fActiveEditor;
		}

		private boolean isCEditor(IWorkbenchPartReference ref) {
			if (ref == null)
				return false;

			String id= ref.getId();
			return CUIPlugin.EDITOR_ID.equals(id) || ref.getPart(false) instanceof CEditor;
		}
	}

	private ASTCache fCache= new ASTCache();
	private ActivationListener fActivationListener;
	private IWorkbenchPart fActiveEditor;
	private long fTimeStamp;

	/**
	 * Returns the C plug-in's AST provider.
	 * 
	 * @return the AST provider
	 */
	public static ASTProvider getASTProvider() {
		return CUIPlugin.getDefault().getASTProvider();
	}

	/**
	 * Creates a new AST provider.
	 */
	public ASTProvider() {
		install();
	}

	/**
	 * Installs this AST provider.
	 */
	void install() {
		if (PlatformUI.isWorkbenchRunning()) {
			// Create and register activation listener
			fActivationListener= new ActivationListener();
			PlatformUI.getWorkbench().addWindowListener(fActivationListener);

			// Ensure existing windows get connected
			IWorkbenchWindow[] windows= PlatformUI.getWorkbench().getWorkbenchWindows();
			for (int i= 0, length= windows.length; i < length; i++)
				windows[i].getPartService().addPartListener(fActivationListener);
		}
	}

	private void activeEditorChanged(IWorkbenchPart editor) {
		ICElement cElement= null;
		if (editor instanceof CEditor) {
			cElement= ((CEditor) editor).getInputCElement();
		}
		synchronized (this) {
			fActiveEditor= editor;
			fTimeStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
			fCache.setActiveElement((ITranslationUnit) cElement);
		}
	}

	/**
	 * Informs that reconciling for the given element is about to be started.
	 *
	 * @param cElement the C element
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#aboutToBeReconciled()
	 */
	void aboutToBeReconciled(ICElement cElement) {
		if (cElement == null)
			return;
		Assert.isTrue(cElement instanceof ITranslationUnit);
		fCache.aboutToBeReconciled((ITranslationUnit) cElement);
		updateModificationStamp();
	}

	private boolean updateModificationStamp() {
		long timeStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
		ITextEditor textEditor= null;
		synchronized (this) {
			if (fActiveEditor instanceof ITextEditor) {
				textEditor= (ITextEditor) fActiveEditor;
				timeStamp= fTimeStamp;
			}
		}
		if (textEditor != null) {
			IEditorInput editorInput= textEditor.getEditorInput();
			IDocument document= textEditor.getDocumentProvider().getDocument(editorInput);
			if (document instanceof IDocumentExtension4) {
				IDocumentExtension4 docExt= (IDocumentExtension4) document;
				long newTimeStamp= docExt.getModificationStamp();
				if (newTimeStamp != timeStamp) {
					synchronized (this) {
						if (fActiveEditor == textEditor && fTimeStamp == timeStamp) {
							fTimeStamp= newTimeStamp;
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Disposes this AST provider.
	 */
	public void dispose() {
		if (fActivationListener != null) {
			// Dispose activation listener
			PlatformUI.getWorkbench().removeWindowListener(fActivationListener);
			fActivationListener= null;
		}
		fCache.setActiveElement(null);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#reconciled()
	 */
	void reconciled(IASTTranslationUnit ast, ICElement cElement, IProgressMonitor progressMonitor) {
		if (cElement == null)
			return;
		Assert.isTrue(cElement instanceof ITranslationUnit);
		fCache.reconciled(ast, (ITranslationUnit) cElement);
	}

	/**
	 * Executes {@link ASTRunnable#runOnAST(ILanguage, IASTTranslationUnit)}
	 * with the shared AST for the given translation unit. Handles acquiring
	 * and releasing the index read-lock for the client.
	 * 
	 * @param cElement     the translation unit
	 * @param waitFlag     condition for waiting for the AST to be built.
	 * @param monitor      a progress monitor, may be <code>null</code>
	 * @param astRunnable  the runnable taking the AST
	 * @return the status  returned by the ASTRunnable
	 */
	public IStatus runOnAST(ICElement cElement, WAIT_FLAG waitFlag, IProgressMonitor monitor,
			ASTCache.ASTRunnable astRunnable) {
		Assert.isTrue(cElement instanceof ITranslationUnit);
		final ITranslationUnit tu = (ITranslationUnit) cElement;
		if (!prepareForUsingCache(tu, waitFlag))
			return Status.CANCEL_STATUS;
		return fCache.runOnAST(tu, waitFlag != WAIT_NO, monitor, astRunnable);
	}

	/**
	 * Returns a shared AST and locks it for exclusive access. An AST obtained from this
	 * method has to be released by calling {@link #releaseSharedAST(IASTTranslationUnit)}.
	 * Subsequent call to this method will block until the AST is released.
	 * <p>
	 * The AST can be released by a thread other than the one that acquired it.
	 * <p>
	 * An index lock must be held by the caller when calling this method. The index lock may
	 * not be released until the AST is released.
	 * 
	 * @param tu The translation unit to get the AST for.
	 * @param index index with read lock held.
	 * @param waitFlag condition for waiting for the AST to be built.
	 * @param monitor a progress monitor, may be <code>null</code>.
	 * @return the shared AST, or <code>null</code> if the shared AST is not available.
	 */
	public final IASTTranslationUnit acquireSharedAST(ITranslationUnit tu, IIndex index,
			WAIT_FLAG waitFlag, IProgressMonitor monitor) {
		if (!prepareForUsingCache(tu, waitFlag))
			return null;
		return fCache.acquireSharedAST(tu, index, waitFlag != WAIT_NO, monitor);
	}

	/**
	 * Releases a shared AST previously acquired by calling
	 * {@link #acquireSharedAST(ITranslationUnit, IIndex, WAIT_FLAG, IProgressMonitor)}.
	 * <p>
	 * Can be called by a thread other than the one that acquired the AST.
	*
	 * @param ast the AST to release.
	 */
	public final void releaseSharedAST(IASTTranslationUnit ast) {
		fCache.releaseSharedAST(ast);
	}

	/**
	 * Prepares the AST cache to be used for the given translation unit.
	 * 
	 * @param tu the translation unit.
	 * @param waitFlag condition for waiting for the AST to be built.
	 * @return <code>true</code> if the AST cache can be used for the given translation unit,
	 * 		<code>false</code> otherwise.
	 */
	private boolean prepareForUsingCache(ITranslationUnit tu, WAIT_FLAG waitFlag) {
		if (!tu.isOpen())
			return false;

		// http://bugs.eclipse.org/bugs/show_bug.cgi?id=342506 explains
		// benign nature of the race conditions in the code below. 
		final boolean isActive= fCache.isActiveElement(tu);
		if (waitFlag == WAIT_ACTIVE_ONLY && !isActive) {
			return false;
		}
		if (isActive && updateModificationStamp()) {
			fCache.disposeAST();
		}
		return true;
	}
}
