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
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.indexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory.CallbackHandler;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory.IndexFileInfo;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A task for index updates.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public class StandaloneFastIndexerTask extends StandaloneIndexerTask implements CallbackHandler {
	private List fChanged = new ArrayList();
	private List fRemoved = new ArrayList();
	private IWritableIndex fIndex;
	private StandaloneIndexBasedCodeReaderFactory fCodeReaderFactory;
	private Map fIflCache;
	private int fCurrentConfigHash= 0;

	public StandaloneFastIndexerTask(StandaloneFastIndexer indexer, List added,
			List changed, List removed) {
		super(indexer);
		fChanged.addAll(added);
		fChanged.addAll(changed);
		fRemoved.addAll(removed);
		updateInfo(0, 0, fChanged.size() + fRemoved.size());
	}

	public void run(IProgressMonitor monitor) throws IOException{
		long start = System.currentTimeMillis();
		try {
			setupIndexAndReaderFactory();
			fIndex.acquireReadLock();
			try {
				registerTUsInReaderFactory(fChanged);

				Iterator i= fRemoved.iterator();
				while (i.hasNext()) {
					if (monitor.isCanceled())
						return;
					String tu = (String) i.next();
					removeTU(fIndex, getIndexFileLocation(tu), 1);
					if (isValidSourceUnitName(tu)) {
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
					String tu = (String) iter.next();
					if (!isValidSourceUnitName(tu)) {
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
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
		traceEnd(start);
	}

	private void setupIndexAndReaderFactory() throws CoreException {
		fIndex= fIndexer.getIndex();
		fIndex.resetCacheCounters();
		fIflCache = new HashMap/*<String,IIndexFileLocation>*/();
		fCodeReaderFactory = new StandaloneIndexBasedCodeReaderFactory(fIndex, fIflCache);
		fCodeReaderFactory.setCallbackHandler(this);
	}

	private void registerTUsInReaderFactory(Collection files) throws IOException, CoreException {
		int removed= 0;
		for (Iterator iter = files.iterator(); iter.hasNext();) {
			String sourcePath = (String) iter.next();
			String path = new File(sourcePath).getCanonicalPath();
			IIndexFileLocation location = getIndexFileLocation(path);
			IndexFileInfo info= fCodeReaderFactory.createFileInfo(location);
			if (updateAll()) {
				info.fRequested= IndexFileInfo.REQUESTED;
			}
			else if (updateChangedTimestamps() && isOutdated(location, info.fFile)) {
				info.fRequested= IndexFileInfo.REQUESTED;
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
			//Standalone indexing stores the absolute paths of files being indexed
			result = new IndexFileLocation(URIUtil.toURI(absolutePath),absolutePath); 
			fIflCache.put(absolutePath, result);
		}
		return result;
	}

	protected IASTTranslationUnit createAST(AbstractLanguage lang, CodeReader codeReader, IScannerInfo scanInfo, int options, IProgressMonitor pm) throws CoreException {
		
		// get the AST in a "Fast" way
		IASTTranslationUnit ast= lang.getASTTranslationUnit(codeReader, scanInfo, fCodeReaderFactory, fIndex, options, fIndexer.getParserLog());
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
