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
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.fast;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory.FileInfo;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Doug Schaefer
 *
 */
abstract class PDOMFastIndexerJob extends PDOMIndexerTask {

	protected final PDOMFastIndexer indexer;
	protected IWritableIndex index;
	protected IndexBasedCodeReaderFactory codeReaderFactory;
	private Map/*<String,IIndexFileLocation>*/ iflCache;

	public PDOMFastIndexerJob(PDOMFastIndexer indexer) throws CoreException {
		this.indexer = indexer;
	}
	
	protected void setupIndexAndReaderFactory() throws CoreException {
		this.index= ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(indexer.getProject());
		this.iflCache = new HashMap/*<String,IIndexFileLocation>*/();
		this.codeReaderFactory = new IndexBasedCodeReaderFactory(index, iflCache);
	}
	
	protected IIndexFileLocation findLocation(String absolutePath) {
		IIndexFileLocation result = (IIndexFileLocation) iflCache.get(absolutePath); 
		if(result==null) {
			result = IndexLocationFactory.getIFLExpensive(absolutePath);
			iflCache.put(absolutePath, result);
		}
		return result;
	}

	protected void registerTUsInReaderFactory(Collection files) throws CoreException {
		for (Iterator iter = files.iterator(); iter.hasNext();) {
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			IIndexFileLocation location = getIndexFileLocation(tu);
			FileInfo info= codeReaderFactory.createFileInfo(location);
			info.setRequested(true);
		}
	}
	
	public IPDOMIndexer getIndexer() {
		return indexer;
	}
	
	protected void doParseTU(ITranslationUnit tu, IProgressMonitor pm) throws CoreException, InterruptedException {
		IPath path = tu.getLocation();
		if (path == null) {
			return;
		}
		ILanguage language = tu.getLanguage();
		if (language == null)
			return;
	
		// skip if no scanner info
		IScannerInfo scanner= tu.getScannerInfo(getIndexAllFiles());
		if (scanner == null) {
			return;
		}
		CodeReader codeReader = tu.getCodeReader();
		if (codeReader == null) {
			return;
		}

		index.acquireReadLock();
		try {
			// get the AST in a "Fast" way
			IASTTranslationUnit ast= language.getASTTranslationUnit(codeReader, scanner, codeReaderFactory, index, ParserUtil.getParserLogService());
			if (pm.isCanceled()) {
				return;
			}
			// Clear the macros
			codeReaderFactory.clearMacroAttachements();
				
			// Add the new symbols
			addSymbols(ast, pm);
		}
		finally {
			index.releaseReadLock();
		}
	}

	protected IWritableIndex getIndex() {
		return index;
	}

	protected int getReadlockCount() {
		return 1;
	}

	protected boolean needToUpdate(IIndexFileLocation location) throws CoreException {
		// file is requested or is not yet indexed.
		FileInfo info= codeReaderFactory.createFileInfo(location);
		return info.isRequested() || info.fFile == null;
	}

	protected boolean postAddToIndex(IIndexFileLocation path, IIndexFile file) throws CoreException {
		FileInfo info= codeReaderFactory.createFileInfo(path);
		info.fFile= file;
		if (info.isRequested()) {
			info.setRequested(false);
			return true;
		}
		return false;
	}
}
