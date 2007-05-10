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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory.CallbackHandler;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory.IndexFileInfo;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

class PDOMFastIndexerTask extends PDOMIndexerTask implements CallbackHandler {
	private List fChanged = new ArrayList();
	private List fRemoved = new ArrayList();
	private IWritableIndex fIndex;
	private IndexBasedCodeReaderFactory fCodeReaderFactory;
	private Map fIflCache;
	private int fCurrentConfigHash= 0;

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
			fIndex.acquireReadLock();
			try {
				registerTUsInReaderFactory();

				Iterator i= fRemoved.iterator();
				while (i.hasNext()) {
					if (monitor.isCanceled())
						return;
					ITranslationUnit tu = (ITranslationUnit)i.next();
					removeTU(fIndex, tu, 1);
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

				parseTUs(fIndex, 1, sources, headers, monitor);
				if (monitor.isCanceled()) {
					return;
				}	
			}
			finally {
				fIndex.releaseReadLock();
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (InterruptedException e) {
		}
		traceEnd(start, fIndex);
	}

	private void setupIndexAndReaderFactory() throws CoreException {
		fIndex= ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(getProject());
		fIndex.resetCacheCounters();
		fIflCache = new HashMap/*<String,IIndexFileLocation>*/();
		fCodeReaderFactory = new IndexBasedCodeReaderFactory(getCProject(), fIndex, fIflCache);
		fCodeReaderFactory.setCallbackHandler(this);
	}

	private void registerTUsInReaderFactory() throws CoreException {
		int removed= 0;
		for (Iterator iter = fChanged.iterator(); iter.hasNext();) {
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			IIndexFileLocation ifl = IndexLocationFactory.getIFL(tu);
			IndexFileInfo info= fCodeReaderFactory.createFileInfo(ifl);
			if (updateAll()) {
				info.fRequested= IndexFileInfo.REQUESTED;
			}
			else if (updateChangedTimestamps() && isOutdated(tu, info.fFile)) {
				info.fRequested= IndexFileInfo.REQUESTED;
			}
			else if (updateChangedConfiguration()) {
				info.fRequested= IndexFileInfo.REQUESTED_IF_CONFIG_CHANGED;
			}
			else {
				iter.remove();
				removed++;
			}
		}
		updateInfo(0, 0, -removed);
	}

	protected IIndexFileLocation findLocation(String absolutePath) {
		IIndexFileLocation result = (IIndexFileLocation) fIflCache.get(absolutePath); 
		if(result==null) {
			result = IndexLocationFactory.getIFLExpensive(getCProject(), absolutePath);
			fIflCache.put(absolutePath, result);
		}
		return result;
	}

	protected IASTTranslationUnit createAST(AbstractLanguage lang, CodeReader codeReader, IScannerInfo scanInfo, int options, IProgressMonitor pm) throws CoreException {
		// get the AST in a "Fast" way
		IASTTranslationUnit ast= lang.getASTTranslationUnit(codeReader, scanInfo, fCodeReaderFactory, fIndex, options, ParserUtil.getParserLogService());
		if (pm.isCanceled()) {
			return null;
		}
		// Clear the macros
		fCodeReaderFactory.clearMacroAttachements();
		return ast;
	}

	protected boolean needToUpdate(IIndexFileLocation location, int confighash) throws CoreException {
		if (super.needToUpdate(location, confighash)) {
			// file is requested or is not yet indexed.
			IndexFileInfo info= fCodeReaderFactory.createFileInfo(location);
			return needToUpdate(info, confighash);
		}
		return false;
	}
	
	public boolean needToUpdate(IndexFileInfo info) throws CoreException {
		return needToUpdate(info, fCurrentConfigHash);
	}
	
	private boolean needToUpdate(IndexFileInfo info, int confighash) throws CoreException {
		if (info.fFile == null) {
			return true;
		}
		if (confighash != 0 && info.fRequested == IndexFileInfo.REQUESTED_IF_CONFIG_CHANGED) {
			int oldhash= info.fFile.getScannerConfigurationHashcode();
			if (oldhash == 0 || oldhash==confighash) {
				info.fRequested= IndexFileInfo.NOT_REQUESTED;
				updateInfo(0, 0, -1);
			}
			else {
				info.fRequested= IndexFileInfo.REQUESTED;
			}
		}
		return info.fRequested != IndexFileInfo.NOT_REQUESTED;
	}

	protected boolean postAddToIndex(IIndexFileLocation path, IIndexFile file)
			throws CoreException {
		IndexFileInfo info= fCodeReaderFactory.createFileInfo(path);
		info.fFile= file;
		if (info.fRequested != IndexFileInfo.NOT_REQUESTED) {
			info.fRequested= IndexFileInfo.NOT_REQUESTED;
			return true;
		}
		return false;
	}
}
