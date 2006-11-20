/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Doug Schaefer
 *
 */
abstract class PDOMFullIndexerJob extends PDOMIndexerTask implements IPDOMIndexerTask {
	final static Object REQUIRED= new Object();
	final static Object MISSING = new Object();
	final static Object SKIP=     new Object();
	
	protected final PDOMFullIndexer indexer;
	protected IWritableIndex index= null;
	private Map filePathsToParse= new HashMap();

	public PDOMFullIndexerJob(PDOMFullIndexer indexer) throws CoreException {
		this.indexer = indexer;
	}

	public IPDOMIndexer getIndexer() {
		return indexer;
	}
	
	protected void setupIndexAndReaderFactory() throws CoreException {
		this.index = ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(indexer.getProject());
	}
	
	protected void registerTUsInReaderFactory(Collection sources) throws CoreException {
		filePathsToParse= new HashMap();
		
		for (Iterator iter = sources.iterator(); iter.hasNext();) {
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			filePathsToParse.put(tu.getLocation().toOSString(), REQUIRED);
		}
	}
	
	protected void doParseTU(ITranslationUnit tu, IProgressMonitor pm) throws CoreException, InterruptedException {
		IPath path = tu.getLocation();
		if (path == null) {
			return;
		}
		int options= 0;
		if (!getIndexAllFiles()) {
			options |= ITranslationUnit.AST_SKIP_IF_NO_BUILD_INFO;
		}
		IASTTranslationUnit ast= tu.getAST(null, options);
		if (ast != null)
			addSymbols(ast, pm);
	}
	
	
	protected IWritableIndex getIndex() {
		return index;
	}

	protected int getReadlockCount() {
		return 0;
	}

	protected boolean needToUpdate(String path) throws CoreException {
		Object required= filePathsToParse.get(path);
		if (required == null) {
			required= MISSING;
			filePathsToParse.put(path, required);
		}
		return required != SKIP;
	}

	protected boolean postAddToIndex(String path, IIndexFile file) throws CoreException {
		Object required= filePathsToParse.get(path);
		filePathsToParse.put(path, SKIP);
		return required == REQUIRED;
	}
}
