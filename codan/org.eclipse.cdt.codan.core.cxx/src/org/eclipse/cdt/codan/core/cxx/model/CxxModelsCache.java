/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.model;

import java.util.WeakHashMap;

import org.eclipse.cdt.codan.core.cxx.internal.model.cfg.CxxControlFlowGraph;
import org.eclipse.cdt.codan.core.model.cfg.IControlFlowGraph;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Cache data models for resource so checkers can share it
 */
public class CxxModelsCache {
	private IFile file;
	private IASTTranslationUnit ast;
	private ITranslationUnit tu;
	private IIndex index;
	private WeakHashMap<IASTFunctionDefinition, IControlFlowGraph> cfgmap = new WeakHashMap<IASTFunctionDefinition, IControlFlowGraph>(0);

	private static CxxModelsCache instance = new CxxModelsCache();

	public static CxxModelsCache getInstance() {
		return instance;
	}

	public synchronized IControlFlowGraph getControlFlowGraph(IASTFunctionDefinition func) {
		IControlFlowGraph cfg = cfgmap.get(func);
		if (cfg!=null) return cfg;
		cfg = CxxControlFlowGraph.build(func);
		cfgmap.put(func, cfg);
		return cfg;
	}
	public synchronized IASTTranslationUnit getAst(IFile file)
			throws CoreException, InterruptedException {
		if (file.equals(this.file)) {
			return ast;
		}

		cfgmap.clear();
		// create translation unit and access index
		ICElement celement = CoreModel.getDefault().create(file);
		if (!(celement instanceof ITranslationUnit))
			return null; // not a C/C++ file
		this.file = file;
		//System.err.println("Making ast for "+file);
		tu = (ITranslationUnit) celement;
		index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
		// lock the index for read access
		index.acquireReadLock();
		try {
			// create index based ast
			ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			if (ast == null)
				return null;//
			return ast;
		} finally {
			index.releaseReadLock();
		}
	}

	public synchronized IIndex getIndex(IFile file)
			throws CoreException, InterruptedException {
		if (file.equals(this.file)) {
			return index;
		}
		getAst(file); // to init variables
		return index;
	}
}
