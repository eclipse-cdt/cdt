/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;

/**
 * Provides a shared AST of a single translation unit at a time.
 *
 * @since 4.0
 */
public class ASTCache {
	/**
	 * Tells whether this class is in debug mode.
	 */
	private static final boolean DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.cdt.core/debug/ASTCache"));  //$NON-NLS-1$//$NON-NLS-2$
	private static final String DEBUG_PREFIX= "[ASTCache] "; //$NON-NLS-1$

	/** Full parse mode (no PDOM) */
	public static int PARSE_MODE_FULL = ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT
			| ITranslationUnit.AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS
			| ITranslationUnit.AST_PARSE_INACTIVE_CODE;

	/** Fast parse mode (use PDOM) */
	public static int PARSE_MODE_FAST = ITranslationUnit.AST_SKIP_ALL_HEADERS
			| ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT
			| ITranslationUnit.AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS
			| ITranslationUnit.AST_PARSE_INACTIVE_CODE;

	/**
	 * Do something with an AST.
	 *
	 * @see #runOnAST(ILanguage, IASTTranslationUnit)
	 */
	public static interface ASTRunnable {
		/**
		 * Do something with the given AST.
		 *
		 * @param lang the language with which the AST has been created.
		 * @param ast  the translation unit AST, may be <code>null</code>
		 * @return a status object
		 */
		IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException;
	}

	private final int fParseMode;
	private final Object fCacheMutex= new Object();

	/** The active translation unit for which to cache the AST */
	private ITranslationUnit fActiveTU;
	/** The cached AST if any */
	private IASTTranslationUnit fAST;
	/**
	 * The timestamp of the last index write access at the time
	 * the AST got cached. A cached AST becomes invalid on any index
	 * write access afterwards.
	 */
	private long fLastWriteOnIndex;
	/** Indicates whether the AST is currently being computed */
	private boolean fIsReconciling;

	/**
	 * Create a new AST cache.
	 */
	public ASTCache() {
		fParseMode= PARSE_MODE_FAST;
	}

	/**
	 * Returns a shared translation unit AST for the given translation unit.
	 * <p>
	 * Clients are not allowed to modify the AST and must hold an index read
	 * lock prior to calling this method and continue to hold the lock as long
	 * as the AST is being used.
	 * </p>
	 *
	 * @param tUnit				the translation unit
	 * @param index				the index used to create the AST, needs to be read-locked
	 * @param wait				if <code>true</code>, wait for AST to be computed (might compute a new AST)
	 * @param progressMonitor	the progress monitor or <code>null</code>
	 * @return					the AST or <code>null</code> if the AST is not available
	 */
	private IASTTranslationUnit getAST(ITranslationUnit tUnit, IIndex index, boolean wait,
			IProgressMonitor progressMonitor) {
		if (tUnit == null)
			return null;

		while (true) {
			if (progressMonitor != null && progressMonitor.isCanceled())
				return null;

			final boolean isActiveElement;
			synchronized (fCacheMutex) {
				isActiveElement= tUnit.equals(fActiveTU);
				if (isActiveElement) {
					if (fAST != null) {
						// AST is cached
						if (fLastWriteOnIndex < index.getLastWriteAccess()) {
							// AST has been invalidated by index write access
							disposeAST();
						} else {
							// cached AST is valid
							if (DEBUG)
								System.out.println(DEBUG_PREFIX + getThreadName() + "returning cached AST:" + toString(fAST) + " for: " + tUnit.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
							return fAST;
						}
					}
					// no cached AST
					if (!wait) {
						// no AST, no wait - we are done
						if (DEBUG)
							System.out.println(DEBUG_PREFIX + getThreadName() + "returning null (WAIT_NO) for: " + tUnit.getElementName()); //$NON-NLS-1$
						return null;
					}
				}
				// no cached AST, but wait
				if (isActiveElement && isReconciling(tUnit)) {
					try {
						// Wait for AST
						if (DEBUG)
							System.out.println(DEBUG_PREFIX + getThreadName() + "waiting for AST for: " + tUnit.getElementName()); //$NON-NLS-1$
						fCacheMutex.wait();
						// Check whether active element is still valid
						if (fAST != null) {
							if (DEBUG)
								System.out.println(DEBUG_PREFIX + getThreadName() + "...got AST for: " + tUnit.getElementName()); //$NON-NLS-1$
							return fAST;
						}
						// try again
						continue;
					} catch (InterruptedException e) {
						return null; // thread has been interrupted don't compute AST
					}
				} else if (!wait) {
					return null;
				}
			}

			if (isActiveElement)
				aboutToBeReconciled(tUnit);

			if (DEBUG)
				System.err.println(DEBUG_PREFIX + getThreadName() + "creating AST for " + tUnit.getElementName()); //$NON-NLS-1$

			IASTTranslationUnit ast= null;
			try {
				ast= createAST(tUnit, index, progressMonitor);
				if (progressMonitor != null && progressMonitor.isCanceled())
					ast= null;
				else if (DEBUG && ast != null)
					System.err.println(DEBUG_PREFIX + getThreadName() + "created AST for: " + tUnit.getElementName()); //$NON-NLS-1$
			} finally {
				if (isActiveElement) {
					if (fAST != null) {
						if (DEBUG)
							System.out.println(DEBUG_PREFIX + getThreadName() + "Ignore created AST for " + tUnit.getElementName() + "- AST from reconciler is newer"); //$NON-NLS-1$ //$NON-NLS-2$
						// other reconciler was faster, still need to trigger notify
						reconciled(fAST, tUnit);
					} else
						reconciled(ast, tUnit);
				}
			}
			return ast;
		}
	}

	/**
	 * Executes {@link ASTRunnable#runOnAST(ILanguage, IASTTranslationUnit)} with the AST
	 * provided by this cache for the given translation unit. Handles acquiring
	 * and releasing the index read-lock for the client.
	 *
	 * @param tUnit
	 *            the translation unit
	 * @param wait
	 *            <code>true</code> if the AST should be computed or waited
	 *            upon in case it is not yet available
	 * @param monitor  a progress monitor, may be <code>null</code>
	 * @param astRunnable  the runnable taking the AST
	 * @return the status returned by the ASTRunnable
	 */
	public IStatus runOnAST(ITranslationUnit tUnit, boolean wait, IProgressMonitor monitor,
			ASTRunnable astRunnable) {
		IIndex index;
		try {
			index = CCorePlugin.getIndexManager().getIndex(tUnit.getCProject(), IIndexManager.ADD_DEPENDENCIES);
			index.acquireReadLock();
		} catch (CoreException e) {
			return e.getStatus();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}

		try {
			IASTTranslationUnit ast= acquireSharedAST(tUnit, index, wait, monitor);
			ILanguage lang= (tUnit instanceof TranslationUnit) ? ((TranslationUnit) tUnit).getLanguageOfContext() : tUnit.getLanguage();
			if (ast == null) {
				return astRunnable.runOnAST(lang, ast);
			}
			try {
				return astRunnable.runOnAST(lang, ast);
			} finally {
				releaseSharedAST(ast);
			}
		} catch (CoreException e) {
			return e.getStatus();
		} finally {
			index.releaseReadLock();
		}
	}

	/**
	 * Returns a shared AST for the given translation unit and locks it for
	 * exclusive access. An AST obtained from this method has to be released
	 * by calling {@link #releaseSharedAST(IASTTranslationUnit)}.
	 * Subsequent call to this method will block until the AST is released.
	 * <p>
	 * The AST can be released by a thread other than the one that acquired it.
	 * <p>
	 * Clients are not allowed to modify the AST and must hold an index read
	 * lock prior to calling this method and continue to hold the lock as long
	 * as the AST is being used.
	 * </p>
	 *
	 * @param tUnit				the translation unit
	 * @param index				the index used to create the AST, needs to be read-locked
	 * @param wait				if <code>true</code>, wait for AST to be computed
	 *                          (might compute a new AST)
	 * @param progressMonitor	the progress monitor or <code>null</code>
	 * @return					the AST or <code>null</code> if the AST is not available
	 */
	public final IASTTranslationUnit acquireSharedAST(ITranslationUnit tUnit, IIndex index,
			boolean wait, IProgressMonitor progressMonitor) {
		IASTTranslationUnit ast= getAST(tUnit, index, wait, progressMonitor);
		if (ast != null) {
			try {
				((ASTTranslationUnit) ast).beginExclusiveAccess();
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			}
		}
		return ast;
	}

	/**
	 * Releases a shared AST previously acquired by calling
	 * {@link #acquireSharedAST(ITranslationUnit, IIndex, boolean, IProgressMonitor)}.
	 * <p>
	 * Can be called by a thread other than the one that acquired the AST.
	*
	 * @param ast   the AST to release.
	 */
	public final void releaseSharedAST(IASTTranslationUnit ast) {
		((ASTTranslationUnit) ast).endExclusiveAccess();
	}

	/**
	 * Caches the given AST for the given translation unit.
	 *
	 * @param ast    the AST
	 * @param tUnit  the translation unit
	 */
	private void cache(IASTTranslationUnit ast, ITranslationUnit tUnit) {
		assert Thread.holdsLock(fCacheMutex);
		if (fActiveTU != null && !fActiveTU.equals(tUnit)) {
			if (DEBUG && tUnit != null) // don't report call from disposeAST()
				System.out.println(DEBUG_PREFIX + getThreadName() + "don't cache AST for inactive: " + toString(tUnit)); //$NON-NLS-1$
			return;
		}

		if (DEBUG && (tUnit != null || ast != null)) // don't report call from disposeAST()
			System.out.println(DEBUG_PREFIX + getThreadName() + "caching AST: " + toString(ast) + " for: " + toString(tUnit)); //$NON-NLS-1$ //$NON-NLS-2$

		if (fAST != null)
			disposeAST();

		fAST= ast;
		fLastWriteOnIndex= fAST == null ? 0 : fAST.getIndex().getLastWriteAccess();

		// Signal AST change
		fCacheMutex.notifyAll();
	}

	/**
	 * Disposes the cached AST.
	 */
	public void disposeAST() {
		synchronized (fCacheMutex) {
			if (fAST == null)
				return;

			if (DEBUG)
				System.out.println(DEBUG_PREFIX + getThreadName() + "disposing AST: " + toString(fAST) + " for: " + toString(fActiveTU)); //$NON-NLS-1$ //$NON-NLS-2$

			fAST= null;
			cache(null, null);
		}
	}

	/**
	 * Creates a new translation unit AST.
	 *
	 * @param tUnit  the translation unit for which to create the AST
	 * @param index  the index for AST generation, needs to be read-locked.
	 * @param progressMonitor  a progress monitor, may be <code>null</code>
	 * @return an AST for the translation unit, or <code>null</code> if the operation was cancelled
	 */
	public IASTTranslationUnit createAST(final ITranslationUnit tUnit, final IIndex index, final IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled())
			return null;

		final IASTTranslationUnit root[]= new IASTTranslationUnit[1];

		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void run() throws CoreException {
				try {
					if (progressMonitor != null && progressMonitor.isCanceled()) {
						root[0]= null;
					} else {
						root[0]= tUnit.getAST(index, fParseMode);
					}
				} catch (OperationCanceledException ex) {
					root[0]= null;
				}
			}
			@Override
			public void handleException(Throwable ex) {
				IStatus status= new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.OK, "Error in CDT Core during AST creation", ex);  //$NON-NLS-1$
				CCorePlugin.getDefault().getLog().log(status);
			}
		});

		return root[0];
	}

	/**
	 * Set the given translation unit as active element to cache an AST for.
	 *
	 * @param tUnit  the translation unit
	 */
	public void setActiveElement(ITranslationUnit tUnit) {
		if (tUnit == fActiveTU) {
			return;
		}
		synchronized (fCacheMutex) {
			fIsReconciling= false;
			fActiveTU= tUnit;
			cache(null, tUnit);
		}
		if (DEBUG)
			System.out.println(DEBUG_PREFIX + getThreadName() + "active element is: " + toString(tUnit)); //$NON-NLS-1$
	}

	/**
	 * Check whether the given translation unit is the active element of this cache.
	 *
	 * @param tUnit  the translation unit
	 * @return  <code>true</code>, if this cache manages the given translation unit
	 */
	public boolean isActiveElement(ITranslationUnit tUnit) {
		synchronized (fCacheMutex) {
			return fActiveTU != null && fActiveTU.equals(tUnit);
		}
	}

	/**
	 * Informs that reconciling (computation of the AST) for the given element
	 * is about to be started.
	 *
	 * @param tUnit  the translation unit
	 */
	public void aboutToBeReconciled(ITranslationUnit tUnit) {
		if (tUnit == null)
			return;

		synchronized (fCacheMutex) {
			if (fActiveTU == null || !fActiveTU.equals(tUnit)) {
				return;
			}

			if (DEBUG)
				System.out.println(DEBUG_PREFIX + getThreadName() + "about to reconcile: " + toString(tUnit)); //$NON-NLS-1$

			fIsReconciling= true;
			cache(null, tUnit);
		}
	}

	/**
	 * Informs that reconciling of the AST of the given translation unit has finished.
	 *
	 * @param ast  the translation unit AST
	 * @param tUnit  the translation unit
	 */
	public void reconciled(IASTTranslationUnit ast, ITranslationUnit tUnit) {
		synchronized (fCacheMutex) {
			if (tUnit == null || !tUnit.equals(fActiveTU)) {
				if (DEBUG)
					System.out.println(DEBUG_PREFIX + getThreadName() + "ignoring AST of out-dated element"); //$NON-NLS-1$
				return;
			}
			if (DEBUG)
				System.out.println(DEBUG_PREFIX + getThreadName() + "reconciled: " + toString(tUnit) + ", AST: " + toString(ast)); //$NON-NLS-1$ //$NON-NLS-2$

			fIsReconciling= false;
			cache(ast, tUnit);
		}
	}

	/**
	 * Tells whether the given C element is the one
	 * reported as currently being reconciled.
	 *
	 * @param tUnit  the translation unit
	 * @return <code>true</code> if reported as currently being reconciled
	 */
	public boolean isReconciling(ITranslationUnit tUnit) {
		synchronized (fCacheMutex) {
			if (fActiveTU == null || tUnit == null) {
				return false;
			}
			return fIsReconciling && (fActiveTU.equals(tUnit));
		}
	}

	private static String getThreadName() {
		String name= Thread.currentThread().getName();
		if (name != null)
			return name + ": "; //$NON-NLS-1$
		else
			return Thread.currentThread().toString() + ": "; //$NON-NLS-1$
	}

	/**
	 * Returns a string for the given C element used for debugging.
	 *
	 * @param tUnit  the translation unit
	 * @return a string used for debugging
	 */
	private static String toString(ITranslationUnit tUnit) {
		if (tUnit == null)
			return "null"; //$NON-NLS-1$
		else
			return tUnit.getElementName();
	}

	/**
	 * Returns a string for the given AST used for debugging.
	 *
	 * @param ast  the translation unit AST
	 * @return a string used for debugging
	 */
	private static String toString(IASTTranslationUnit ast) {
		if (ast == null)
			return "null"; //$NON-NLS-1$

		return ast.getFilePath();
	}
}
