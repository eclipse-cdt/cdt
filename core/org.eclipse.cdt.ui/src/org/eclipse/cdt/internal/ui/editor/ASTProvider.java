/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;


/**
 * Provides a shared AST for clients. The shared AST is
 * the AST of the active CEditor's input element.
 * Cloned from JDT.
 * 
 * @since 4.0
 */
public final class ASTProvider {

	public static interface ASTRunnable {
		IStatus runOnAST(IASTTranslationUnit tu);
	}
	
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
		public String toString() {
			return fName;
		}
	}

	/**
	 * Wait flag indicating that a client requesting an AST
	 * wants to wait until an AST is ready.
	 * <p>
	 * An AST will be created by this AST provider if the shared
	 * AST is not for the given C element.
	 * </p>
	 */
	public static final WAIT_FLAG WAIT_YES= new WAIT_FLAG("wait yes"); //$NON-NLS-1$

	/**
	 * Wait flag indicating that a client requesting an AST
	 * only wants to wait for the shared AST of the active editor.
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
	 * Tells whether this class is in debug mode.
	 */
	private static final boolean DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.cdt.ui/debug/ASTProvider"));  //$NON-NLS-1$//$NON-NLS-2$

	/**
	 * Internal activation listener.
	 */
	private class ActivationListener implements IPartListener2, IWindowListener {

		/*
		 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partActivated(IWorkbenchPartReference ref) {
			if (isCEditor(ref) && !isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partBroughtToTop(IWorkbenchPartReference ref) {
			if (isCEditor(ref) && !isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partClosed(IWorkbenchPartReference ref) {
			if (isActiveEditor(ref)) {
				if (DEBUG)
					System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "closed active editor: " + ref.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$

				activeEditorChanged(null);
			}
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partDeactivated(IWorkbenchPartReference ref) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partOpened(IWorkbenchPartReference ref) {
			if (isCEditor(ref) && !isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partHidden(IWorkbenchPartReference ref) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partVisible(IWorkbenchPartReference ref) {
			if (isCEditor(ref) && !isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partInputChanged(IWorkbenchPartReference ref) {
			if (isCEditor(ref) && isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowActivated(IWorkbenchWindow window) {
			IWorkbenchPartReference ref= window.getPartService().getActivePartReference();
			if (isCEditor(ref) && !isActiveEditor(ref))
				activeEditorChanged(ref.getPart(true));
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowDeactivated(IWorkbenchWindow window) {
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowClosed(IWorkbenchWindow window) {
			if (fActiveEditor != null && fActiveEditor.getSite() != null && window == fActiveEditor.getSite().getWorkbenchWindow()) {
				if (DEBUG)
					System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "closed active editor: " + fActiveEditor.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$

				activeEditorChanged(null);
			}
			window.getPartService().removePartListener(this);
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowOpened(IWorkbenchWindow window) {
			window.getPartService().addPartListener(this);
		}

		private boolean isActiveEditor(IWorkbenchPartReference ref) {
			return ref != null && isActiveEditor(ref.getPart(false));
		}

		private boolean isActiveEditor(IWorkbenchPart part) {
			return part != null && (part == fActiveEditor);
		}

		private boolean isCEditor(IWorkbenchPartReference ref) {
			if (ref == null)
				return false;

			String id= ref.getId();

			return CUIPlugin.EDITOR_ID.equals(id) || ref.getPart(false) instanceof CEditor;
		}
	}

	private static final String DEBUG_PREFIX= "ASTProvider > "; //$NON-NLS-1$


	private ICElement fReconcilingCElement;
	private ICElement fActiveCElement;
	private IPositionConverter fActivePositionConverter;
	private IASTTranslationUnit fAST;
	private ActivationListener fActivationListener;
	private Object fReconcileLock= new Object();
	private Object fWaitLock= new Object();
	private boolean fIsReconciling;
	private IWorkbenchPart fActiveEditor;

	protected int fParseMode= PARSE_MODE_FAST;

	private long fLastWriteOnIndex= -1;

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
			cElement= ((CEditor)editor).getInputCElement();
		}
		
		synchronized (this) {
			fActiveEditor= editor;
			fActiveCElement= cElement;
			cache(null, null, cElement);
		}

		if (DEBUG)
			System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "active editor is: " + toString(cElement)); //$NON-NLS-1$ //$NON-NLS-2$

		synchronized (fReconcileLock) {
			if (fIsReconciling && (fReconcilingCElement == null || !fReconcilingCElement.equals(cElement))) {
				fIsReconciling= false;
				fReconcilingCElement= null;
			} else if (cElement == null) {
				fIsReconciling= false;
				fReconcilingCElement= null;
			}
		}
	}

	/**
	 * Returns whether the given translation unit AST is
	 * cached by this AST provided.
	 *
	 * @param ast the translation unit AST
	 * @return <code>true</code> if the given AST is the cached one
	 */
	public boolean isCached(IASTTranslationUnit ast) {
		return ast != null && fAST == ast;
	}

	/**
	 * Returns whether this AST provider is active on the given
	 * translation unit.
	 *
	 * @param tu the translation unit
	 * @return <code>true</code> if the given translation unit is the active one
	 */
	public boolean isActive(ITranslationUnit tu) {
		return tu != null && tu.equals(fActiveCElement);
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

		if (DEBUG)
			System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "about to reconcile: " + toString(cElement)); //$NON-NLS-1$ //$NON-NLS-2$

		synchronized (fReconcileLock) {
			fIsReconciling= true;
			fReconcilingCElement= cElement;
		}
		cache(null, null, cElement);
	}

	/**
	 * Disposes the cached AST.
	 */
	private synchronized void disposeAST() {

		if (fAST == null)
			return;

		if (DEBUG)
			System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "disposing AST: " + toString(fAST) + " for: " + toString(fActiveCElement)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		fAST= null;

		cache(null, null, null);
	}

	/**
	 * Returns a string for the given C element used for debugging.
	 *
	 * @param cElement the translation unit AST
	 * @return a string used for debugging
	 */
	private String toString(ICElement cElement) {
		if (cElement == null)
			return "null"; //$NON-NLS-1$
		else
			return cElement.getElementName();

	}

	/**
	 * Returns a string for the given AST used for debugging.
	 *
	 * @param ast the translation unit AST
	 * @return a string used for debugging
	 */
	private String toString(IASTTranslationUnit ast) {
		if (ast == null)
			return "null"; //$NON-NLS-1$

		IASTNode[] nodes= ast.getDeclarations();
		if (nodes != null && nodes.length > 0)
			return nodes[0].getRawSignature();
		else
			return "AST without any declaration"; //$NON-NLS-1$
	}

	/**
	 * Caches the given translation unit AST for the given C element.
	 *
	 * @param ast
	 * @param cElement
	 */
	private synchronized void cache(IASTTranslationUnit ast, IPositionConverter converter, ICElement cElement) {

		if (fActiveCElement != null && !fActiveCElement.equals(cElement)) {
			if (DEBUG && cElement != null) // don't report call from disposeAST()
				System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "don't cache AST for inactive: " + toString(cElement)); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		if (DEBUG && (cElement != null || ast != null)) // don't report call from disposeAST()
			System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "caching AST: " + toString(ast) + " for: " + toString(cElement)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		if (fAST != null)
			disposeAST();

		fAST= ast;
		fLastWriteOnIndex= fAST == null ? 0 : fAST.getIndex().getLastWriteAccess();
		fActivePositionConverter= converter;

		// Signal AST change
		synchronized (fWaitLock) {
			fWaitLock.notifyAll();
		}
	}

	/**
	 * Returns a shared translation unit AST for the given
	 * C element.
	 * <p>
	 * Clients are not allowed to modify the AST and must
	 * synchronize all access to its nodes.
	 * </p>
	 *
	 * @param cElement				the C element
	 * @param index				the index used to create the AST, needs to be read-locked.
	 * @param waitFlag			{@link #WAIT_YES}, {@link #WAIT_NO} or {@link #WAIT_ACTIVE_ONLY}
	 * @param progressMonitor	the progress monitor or <code>null</code>
	 * @return					the AST or <code>null</code> if the AST is not available
	 */
	public IASTTranslationUnit getAST(ICElement cElement, IIndex index, WAIT_FLAG waitFlag, IProgressMonitor progressMonitor) {
		if (cElement == null)
			return null;
		
		Assert.isTrue(cElement instanceof ITranslationUnit);

		if (progressMonitor != null && progressMonitor.isCanceled())
			return null;

		boolean isActiveElement;
		synchronized (this) {
			isActiveElement= cElement.equals(fActiveCElement);
			if (isActiveElement) {
				if (fAST != null) {
					if (fLastWriteOnIndex < index.getLastWriteAccess()) {
						disposeAST();
					}
					else {
						if (DEBUG)
							System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "returning cached AST:" + toString(fAST) + " for: " + cElement.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

						return fAST;
					}
				}
				if (waitFlag == WAIT_NO) {
					if (DEBUG)
						System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "returning null (WAIT_NO) for: " + cElement.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$

					return null;
				}
			}
		}
		if (isActiveElement && isReconciling(cElement)) {
			try {
				final ICElement activeElement= fReconcilingCElement;

				// Wait for AST
				synchronized (fWaitLock) {
					if (DEBUG)
						System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "waiting for AST for: " + cElement.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$

					// don't wait forever, notify might have happened already
					fWaitLock.wait(1000);
				}

				// Check whether active element is still valid
				synchronized (this) {
					if (activeElement == fActiveCElement && fAST != null) {
						if (DEBUG)
							System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "...got AST for: " + cElement.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$

						return fAST;
					}
				}
				return getAST(cElement, index, waitFlag, progressMonitor);
			} catch (InterruptedException e) {
				return null; // thread has been interrupted don't compute AST
			}
		} else if (waitFlag == WAIT_NO || (waitFlag == WAIT_ACTIVE_ONLY && !(isActiveElement && fAST == null)))
			return null;

		if (isActiveElement)
			aboutToBeReconciled(cElement);

		if (DEBUG)
			System.err.println(getThreadName() + " - " + DEBUG_PREFIX + "creating AST for " + cElement.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$

		IASTTranslationUnit ast= null;
		try {
			ast= createAST(cElement, index, progressMonitor);
			if (progressMonitor != null && progressMonitor.isCanceled())
				ast= null;
			else if (DEBUG && ast != null)
				System.err.println(getThreadName() + " - " + DEBUG_PREFIX + "created AST for: " + cElement.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			if (isActiveElement) {
				if (fAST != null) {
					if (DEBUG)
						System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "Ignore created AST for " + cElement.getElementName() + "- AST from reconciler is newer"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					reconciled(fAST, fActivePositionConverter, cElement, null);
				} else
					reconciled(ast, null, cElement, null);
			}
		}
		return ast;
	}

	/**
	 * Tells whether the given C element is the one
	 * reported as currently being reconciled.
	 *
	 * @param cElement the C element
	 * @return <code>true</code> if reported as currently being reconciled
	 */
	private boolean isReconciling(ICElement cElement) {
		synchronized (fReconcileLock) {
			return cElement != null && cElement.equals(fReconcilingCElement) && fIsReconciling;
		}
	}

	/**
	 * Creates a new translation unit AST.
	 *
	 * @param cElement the C element for which to create the AST
	 * @param index for AST generation, needs to be read-locked.
	 * @param progressMonitor the progress monitor
	 * @return AST
	 */
	IASTTranslationUnit createAST(ICElement cElement, final IIndex index, final IProgressMonitor progressMonitor) {
		if (!hasSource(cElement))
			return null;
		
		if (progressMonitor != null && progressMonitor.isCanceled())
			return null;

		if (!(cElement instanceof ITranslationUnit))
			return null;

		final ITranslationUnit tu= (ITranslationUnit)cElement;
		final IASTTranslationUnit root[]= new IASTTranslationUnit[1]; 
		
		SafeRunner.run(new ISafeRunnable() {
			public void run() throws CoreException {
				try {
					if (progressMonitor != null && progressMonitor.isCanceled()) {
						root[0]= null;
					} else {
						root[0]= tu.getAST(index, fParseMode);
					}
				} catch (OperationCanceledException ex) {
					root[0]= null;
				}
			}
			public void handleException(Throwable ex) {
				IStatus status= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.OK, "Error in CDT Core during AST creation", ex);  //$NON-NLS-1$
				CUIPlugin.getDefault().getLog().log(status);
			}
		});
			
		return root[0];
	}
	
	/**
	 * Checks whether the given C element has accessible source.
	 * 
	 * @param cElement the C element to test
	 * @return <code>true</code> if the element has source
	 */
	private boolean hasSource(ICElement cElement) {
		if (cElement == null || !cElement.exists())
			return false;
		
		try {
			return cElement instanceof ISourceReference /* && ((ISourceReference)cElement).getSource() != null */;
		} catch (Exception ex) {
			IStatus status= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.OK, "Error in CDT Core during AST creation", ex);  //$NON-NLS-1$
			CUIPlugin.getDefault().getLog().log(status);
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

		disposeAST();

		synchronized (fWaitLock) {
			fWaitLock.notifyAll();
		}
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#reconciled()
	 */
	void reconciled(IASTTranslationUnit ast, IPositionConverter converter, ICElement cElement, IProgressMonitor progressMonitor) {

		if (DEBUG)
			System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "reconciled: " + toString(cElement) + ", AST: " + toString(ast)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		synchronized (fReconcileLock) {

			fIsReconciling= progressMonitor != null && progressMonitor.isCanceled();
			if (cElement == null || !cElement.equals(fReconcilingCElement)) {

				if (DEBUG)
					System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "  ignoring AST of out-dated editor"); //$NON-NLS-1$ //$NON-NLS-2$

				// Signal - threads might wait for wrong element
				synchronized (fWaitLock) {
					fWaitLock.notifyAll();
				}

				return;
			}

			cache(ast, converter, cElement);
		}
	}

	private static String getThreadName() {
		String name= Thread.currentThread().getName();
		if (name != null)
			return name;
		else
			return Thread.currentThread().toString();
	}

	/**
	 * @param element
	 * @return the position converter for the AST of the active element or <code>null</code>
	 */
	public IPositionConverter getActivePositionConverter(ICElement element) {
		if (fActiveCElement == element) {
			return fActivePositionConverter;
		}
		return null;
	}

	public IStatus runOnAST(ICElement cElement, WAIT_FLAG waitFlag, IProgressMonitor monitor,
			ASTRunnable astRunnable) {
		IIndex index;
		try {
			index = CCorePlugin.getIndexManager().getIndex(cElement.getCProject());
			index.acquireReadLock();
		} catch (CoreException e) {
			return e.getStatus();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		
		try {
			IASTTranslationUnit ast= getAST(cElement, index, waitFlag, monitor);
			return astRunnable.runOnAST(ast);
		}
		finally {
			index.releaseReadLock();
		}
	}
}

