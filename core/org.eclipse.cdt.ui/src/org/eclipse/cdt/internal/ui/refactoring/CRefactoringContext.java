/*******************************************************************************
 * Copyright (c) 2010, 2012 Google, Inc and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringContext;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;

/**
 * A disposable context for C/C++ refactoring operations. Contains cache of ASTs of the translation
 * units participating in refactoring. The context object has to be disposed of after use. Failure
 * to do so may cause loss of index lock.
 * <p>
 * This class is not thread-safe.
 */
public class CRefactoringContext extends RefactoringContext {
	private static final int PARSE_MODE = ITranslationUnit.AST_SKIP_ALL_HEADERS
			| ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT
			| ITranslationUnit.AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS
			| ITranslationUnit.AST_PARSE_INACTIVE_CODE;

	private final Map<ITranslationUnit, IASTTranslationUnit> fASTCache;
	private IIndex fIndex;
	private IASTTranslationUnit fSharedAST;

	public CRefactoringContext(CRefactoring refactoring) {
		super(refactoring);
		refactoring.setContext(this);
		fASTCache = new ConcurrentHashMap<ITranslationUnit, IASTTranslationUnit>();
	}

	/**
	 * Returns an AST for the given translation unit. The AST is built for the working
	 * copy of the translation unit if such working copy exists. The returned AST is
	 * a shared one whenever possible.
	 * <p>
	 * An AST returned by this method should not be accessed concurrently by multiple threads.
	 * <p>
	 * <b>NOTE</b>: No references to the AST or its nodes can be kept after calling
	 * the {@link #dispose()} method.
	 *
	 * @param tu The translation unit.
	 * @param pm A progress monitor.
	 * @return An AST, or <code>null</code> if the AST cannot be obtained.
	 */
	public IASTTranslationUnit getAST(ITranslationUnit tu, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		if (isDisposed())
			throw new IllegalStateException("CRefactoringContext is already disposed."); //$NON-NLS-1$
        getIndex();  // Make sure the index is locked.
		if (pm != null && pm.isCanceled())
			throw new OperationCanceledException();

		tu= CModelUtil.toWorkingCopy(tu);
    	// Try to get a shared AST before creating our own.
    	IASTTranslationUnit ast= fASTCache.get(tu);
    	if (ast == null) {
    		if (fSharedAST != null && tu.equals(fSharedAST.getOriginatingTranslationUnit())) {
    			ast = fSharedAST;
    		} else {
	        	ast = ASTProvider.getASTProvider().acquireSharedAST(tu, fIndex,
	        			ASTProvider.WAIT_ACTIVE_ONLY, pm);
	        	if (ast == null) {
					if (pm != null && pm.isCanceled())
						throw new OperationCanceledException();
					ast= tu.getAST(fIndex, PARSE_MODE);
		        	fASTCache.put(tu, ast);
	        	} else {
	        		if (fSharedAST != null) {
	        			ASTProvider.getASTProvider().releaseSharedAST(fSharedAST);
	        		}
	        		fSharedAST = ast;
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
	public IIndex getIndex() throws CoreException, OperationCanceledException {
		if (isDisposed())
			throw new IllegalStateException("CRefactoringContext is already disposed."); //$NON-NLS-1$
		if (fIndex == null) {
			ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
			IIndex index = CCorePlugin.getIndexManager().getIndex(projects,
					IIndexManager.ADD_EXTENSION_FRAGMENTS_EDITOR);
			try {
				index.acquireReadLock();
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			}
			fIndex = index;
		}
		return fIndex;
	}

	@Override
	public void dispose() {
		if (isDisposed())
			throw new IllegalStateException("CRefactoringContext.dispose() called more than once."); //$NON-NLS-1$
		if (fSharedAST != null) {
			ASTProvider.getASTProvider().releaseSharedAST(fSharedAST);
		}
		if (fIndex != null) {
			fIndex.releaseReadLock();
		}
		super.dispose();
	}

	private boolean isDisposed() {
		return getRefactoring() == null;
	}

	@Override
	protected void finalize() throws Throwable {
		if (!isDisposed())
			CUIPlugin.logError("CRefactoringContext was not disposed"); //$NON-NLS-1$
		super.finalize();
	}
}
