/*******************************************************************************
 * Copyright (c) 2010, 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.services.IDisposable;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;

/**
 * Cache containing ASTs for the translation units participating in refactoring.
 * The cache object has to be disposed of after use. Failure to do so may cause
 * loss of index lock.
 * 
 * This class is thread-safe.
 */
public class RefactoringASTCache implements IDisposable {
	private final Map<ITranslationUnit, IASTTranslationUnit> fASTCache;
	private final Object astBuildMutex;
	private IIndex fIndex;
	private boolean fDisposed;

	public RefactoringASTCache() {
		fASTCache = new ConcurrentHashMap<ITranslationUnit, IASTTranslationUnit>();
		astBuildMutex = new Object();
	}

	/**
	 * Returns an AST for the given translation unit. The AST is built for the working
	 * copy of the translation unit if such working copy exists. The returned AST is
	 * a shared one whenever possible.
	 * NOTE: No references to the AST or its nodes can be kept after calling
	 * the {@link #dispose()} method. 
	 * @param tu The translation unit.
	 * @param pm A progress monitor.
	 * @return An AST, or <code>null</code> if the AST cannot be obtained.
	 */
	public IASTTranslationUnit getAST(ITranslationUnit tu, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
        Assert.isTrue(!fDisposed, "RefactoringASTCache is already disposed"); //$NON-NLS-1$
        getIndex();  // Make sure the index is locked.

    	tu= CModelUtil.toWorkingCopy(tu);
    	IASTTranslationUnit ast;
		ast= fASTCache.get(tu);

        if (ast == null) {
        	// Try to get a shared AST before creating our own.
        	final IASTTranslationUnit[] astHolder = new IASTTranslationUnit[1];
			ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_IF_OPEN, pm, new ASTRunnable() {
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
					// Leaking of AST outside of runOnAST method is dangerous, but it does not cause
					// harm here since the index remains locked for the duration of the AST life span.
					astHolder[0] = ast;
					return Status.OK_STATUS;
				}
			});
			ast = astHolder[0];

        	if (ast == null) {
				synchronized (astBuildMutex) {
					ast= fASTCache.get(tu);
					if (ast == null) {
						int options= ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT |
								ITranslationUnit.AST_SKIP_INDEXED_HEADERS;
						ast= tu.getAST(fIndex, options);
		            	fASTCache.put(tu, ast);
					}
				}
			}
        }
        if (pm != null) {
        	pm.done();
        }
        return ast;
    }

	/**
	 * Returns the index that can be safely used for reading until the cache is disposed.
	 * 
	 * @return The index.
	 */
	public synchronized IIndex getIndex() throws CoreException, OperationCanceledException {
        Assert.isTrue(!fDisposed, "RefactoringASTCache is already disposed"); //$NON-NLS-1$
		if (fIndex == null) {
			ICProject[] projects;
			projects = CoreModel.getDefault().getCModel().getCProjects();
			IIndex index = CCorePlugin.getIndexManager().getIndex(projects);
			try {
				index.acquireReadLock();
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			}
			fIndex = index;
		}
		return fIndex;
	}

	/**
	 * @see IDisposable#dispose()
	 * This method should not be called concurrently with any other method.
	 */
	public void dispose() {
        Assert.isTrue(!fDisposed, "RefactoringASTCache.dispose() called more than once"); //$NON-NLS-1$
		fDisposed = true;
		if (fIndex != null) {
			fIndex.releaseReadLock();
		}
	}

	@Override
	protected void finalize() throws Throwable {
        Assert.isTrue(fDisposed, "RefactoringASTCache was not disposed"); //$NON-NLS-1$
		super.finalize();
	}
}
