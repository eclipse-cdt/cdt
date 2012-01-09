/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.model;

import java.util.WeakHashMap;

import org.eclipse.cdt.codan.core.cxx.Activator;
import org.eclipse.cdt.codan.core.cxx.internal.model.CodanCommentMap;
import org.eclipse.cdt.codan.core.cxx.internal.model.cfg.CxxControlFlowGraph;
import org.eclipse.cdt.codan.core.model.ICodanDisposable;
import org.eclipse.cdt.codan.core.model.cfg.IControlFlowGraph;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Cache data models for resource so checkers can share it
 */
public class CxxModelsCache implements ICodanDisposable {
	private static final int PARSE_MODE = ITranslationUnit.AST_SKIP_ALL_HEADERS
			| ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT
			| ITranslationUnit.AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS
			| ITranslationUnit.AST_PARSE_INACTIVE_CODE;

	private final IFile file;
	private final ITranslationUnit tu;
	private IASTTranslationUnit ast;
	private IIndex index;
	private final WeakHashMap<IASTFunctionDefinition, IControlFlowGraph> cfgmap;
	private ICodanCommentMap commentMap;
	private boolean disposed;

	CxxModelsCache(ITranslationUnit tu) {
		this.tu = tu;
		this.file = tu != null ? (IFile) tu.getResource() : null;
		cfgmap = new WeakHashMap<IASTFunctionDefinition, IControlFlowGraph>(0);
	}
	
	CxxModelsCache(IASTTranslationUnit ast) {
		this(ast.getOriginatingTranslationUnit());
		this.ast = ast;
	}

	public IASTTranslationUnit getAST() throws OperationCanceledException, CoreException {
		return getAST(tu);
	}

	public IASTTranslationUnit getAST(ITranslationUnit tu)
			throws OperationCanceledException, CoreException {
		if (!this.tu.equals(tu)) {
			throw new IllegalArgumentException();
		}
		if (ast == null) {
			getIndex();
			ast= tu.getAST(index, PARSE_MODE);
		}
		return ast;
	}

	public ITranslationUnit getTranslationUnit() {
		return tu;
	}

	public IFile getFile() {
		return file;
	}

	public synchronized IControlFlowGraph getControlFlowGraph(IASTFunctionDefinition func) {
		IControlFlowGraph cfg = cfgmap.get(func);
		if (cfg != null)
			return cfg;
		cfg = CxxControlFlowGraph.build(func);
		// TODO(Alena Laskavaia): Change to LRU.
		if (cfgmap.size() > 20) { // if too many function better drop the cash
			cfgmap.clear();
		}
		cfgmap.put(func, cfg);
		return cfg;
	}

	public synchronized ICodanCommentMap getCommentedNodeMap() {
		return getCommentedNodeMap(tu);
	}
	
	public synchronized ICodanCommentMap getCommentedNodeMap(ITranslationUnit tu) {
		if (!this.tu.equals(tu)) {
			throw new IllegalArgumentException();
		}
		if (commentMap == null) {
			if (ast == null) {
				throw new IllegalStateException("getCommentedNodeMap called before getAST"); //$NON-NLS-1$
			}
			commentMap = new CodanCommentMap(ASTCommenter.getCommentedNodeMap(ast));
		}
		return commentMap;
	}

	/**
	 * Returns the index that can be safely used for reading until the cache is disposed.
	 * 
	 * @return The index.
	 */
	public synchronized IIndex getIndex() throws CoreException, OperationCanceledException {
        Assert.isTrue(!disposed, "CxxASTCache is already disposed."); //$NON-NLS-1$
		if (this.index == null) {
			ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
			IIndex index = CCorePlugin.getIndexManager().getIndex(projects);
			try {
				index.acquireReadLock();
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			}
			this.index = index;
		}
		return this.index;
	}

	/**
	 * @see IDisposable#dispose()
	 * This method should not be called concurrently with any other method.
	 */
	@Override
	public void dispose() {
        Assert.isTrue(!disposed, "CxxASTCache.dispose() called more than once."); //$NON-NLS-1$
		disposed = true;
		if (index != null) {
			index.releaseReadLock();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (!disposed)
			Activator.log("CxxASTCache was not disposed."); //$NON-NLS-1$
		super.finalize();
	}
}
