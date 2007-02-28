/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.fast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
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

class PDOMFastIndexerTask extends PDOMIndexerTask {
	private List fChanged = new ArrayList();
	private List fRemoved = new ArrayList();
	private IWritableIndex index;
	private IndexBasedCodeReaderFactory fCodeReaderFactory;
	private Map fIflCache;

	public PDOMFastIndexerTask(PDOMFastIndexer indexer, ITranslationUnit[] added,
			ITranslationUnit[] changed, ITranslationUnit[] removed) {
		super(indexer);
		fChanged.addAll(Arrays.asList(added));
		fChanged.addAll(Arrays.asList(changed));
		fRemoved.addAll(Arrays.asList(removed));
		updateInfo(0, 0, fChanged.size() + fRemoved.size());
	}

	public void run(IProgressMonitor monitor) {
		long start = System.currentTimeMillis();
		try {
			setupIndexAndReaderFactory();
			index.acquireReadLock();
			try {
				registerTUsInReaderFactory(fChanged);

				Iterator i= fRemoved.iterator();
				while (i.hasNext()) {
					if (monitor.isCanceled())
						return;
					ITranslationUnit tu = (ITranslationUnit)i.next();
					removeTU(index, tu, 1);
					if (tu.isSourceUnit()) {
						updateInfo(1, 0, 0);
					}
					else {
						updateInfo(0, 1, -1);
					}
				}

				// separate headers
				List headers= new ArrayList();
				List sources= fChanged;
				for (Iterator iter = fChanged.iterator(); iter.hasNext();) {
					ITranslationUnit tu = (ITranslationUnit) iter.next();
					if (!tu.isSourceUnit()) {
						headers.add(tu);
						iter.remove();
					}
				}

				parseTUs(index, 1, sources, headers, monitor);
				if (monitor.isCanceled()) {
					return;
				}	
			}
			finally {
				index.releaseReadLock();
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (InterruptedException e) {
		}
		traceEnd(start);
	}

	private void setupIndexAndReaderFactory() throws CoreException {
		this.index= ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(getProject());
		this.fIflCache = new HashMap/*<String,IIndexFileLocation>*/();
		this.fCodeReaderFactory = new IndexBasedCodeReaderFactory(index, fIflCache);
	}

	private void registerTUsInReaderFactory(Collection files) throws CoreException {
		for (Iterator iter = files.iterator(); iter.hasNext();) {
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			IIndexFileLocation location = getIndexFileLocation(tu);
			FileInfo info= fCodeReaderFactory.createFileInfo(location);
			info.setRequested(true);
		}
	}

	protected IIndexFileLocation findLocation(String absolutePath) {
		IIndexFileLocation result = (IIndexFileLocation) fIflCache.get(absolutePath); 
		if(result==null) {
			result = IndexLocationFactory.getIFLExpensive(absolutePath);
			fIflCache.put(absolutePath, result);
		}
		return result;
	}

	protected IASTTranslationUnit createAST(ITranslationUnit tu, IProgressMonitor pm) throws CoreException,
			InterruptedException {
		IPath path = tu.getLocation();
		if (path == null) {
			return null;
		}
		ILanguage language = tu.getLanguage();
		if (language == null)
			return null;

		// skip if no scanner info
		IScannerInfo scanner= tu.getScannerInfo(getIndexAllFiles());
		if (scanner == null) {
			return null;
		}
		CodeReader codeReader = tu.getCodeReader();
		if (codeReader == null) {
			return null;
		}

		// get the AST in a "Fast" way
		IASTTranslationUnit ast= language.getASTTranslationUnit(codeReader, scanner, fCodeReaderFactory, index, ParserUtil.getParserLogService());
		if (pm.isCanceled()) {
			return null;
		}
		// Clear the macros
		fCodeReaderFactory.clearMacroAttachements();

		return ast;
	}

	protected boolean needToUpdate(IIndexFileLocation location) throws CoreException {
		// file is requested or is not yet indexed.
		FileInfo info= fCodeReaderFactory.createFileInfo(location);
		return info.isRequested() || info.fFile == null;
	}

	protected boolean postAddToIndex(IIndexFileLocation path, IIndexFile file)
			throws CoreException {
		FileInfo info= fCodeReaderFactory.createFileInfo(path);
		info.fFile= file;
		if (info.isRequested()) {
			info.setRequested(false);
			return true;
		}
		return false;
	}
}
