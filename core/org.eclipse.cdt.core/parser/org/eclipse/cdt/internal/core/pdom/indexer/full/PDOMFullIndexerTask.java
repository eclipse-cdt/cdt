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

package org.eclipse.cdt.internal.core.pdom.indexer.full;

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
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.dom.SavedCodeReaderFactory;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Doug Schaefer
 *
 */
class PDOMFullIndexerTask extends PDOMIndexerTask {
	private final static Object REQUIRED= new Object();
	private final static Object MISSING = new Object();
	private final static Object SKIP=     new Object();
	private final static Object REQUIRED_IF_CONFIG_CHANGED= new Object();
	
	private List fChanged = new ArrayList();
	private List fRemoved = new ArrayList();
	private IWritableIndex fIndex = null;
	private Map filePathsToParse = new HashMap/*<IIndexFileLocation, Object>*/();
	private Map fIflCache = new HashMap/*<String, IIndexFileLocation>*/();

	public PDOMFullIndexerTask(PDOMFullIndexer indexer, ITranslationUnit[] added,
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
			setupIndex();
			registerTUsInReaderFactory(fChanged);
			
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
					
			Iterator i= fRemoved.iterator();
			while (i.hasNext()) {
				if (monitor.isCanceled())
					return;
				ITranslationUnit tu = (ITranslationUnit)i.next();
				removeTU(fIndex, tu, 0);
				if (tu.isSourceUnit()) {
					updateInfo(1, 0, 0);
				}
				else {
					updateInfo(0, 1, -1);
				}
			}

			fIndex.acquireReadLock();
			try {
				parseTUs(fIndex, 1, sources, headers, monitor);
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

	private void setupIndex() throws CoreException {
		// there is no mechanism to clear dirty files from the cache, so flush it.
		SavedCodeReaderFactory.getInstance().getCodeReaderCache().flush();	

		fIndex = ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(getProject());
		fIndex.resetCacheCounters();
	}

	private void registerTUsInReaderFactory(Collection/*<ITranslationUnit>*/ sources)
			throws CoreException {
		int removed= 0;
		filePathsToParse= new HashMap/*<IIndexFileLocation, Object>*/();
		for (Iterator iter = sources.iterator(); iter.hasNext();) {
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			IIndexFileLocation ifl= IndexLocationFactory.getIFL(tu);
			if (updateAll()) {
				filePathsToParse.put(ifl, REQUIRED);
			}
			else if (updateChangedTimestamps() && isOutdated(tu, fIndex.getFile(ifl))) {
				filePathsToParse.put(ifl, REQUIRED);
			}
			else if (updateChangedConfiguration()) {
				filePathsToParse.put(ifl, REQUIRED_IF_CONFIG_CHANGED);
			}
			else {
				iter.remove();
				removed++;
				continue;
			}
			updateInfo(0, 0, -removed);
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

	protected IASTTranslationUnit createAST(AbstractLanguage lang, CodeReader codeReader, IScannerInfo scanInfo, int options, IProgressMonitor pm) throws CoreException {
		SavedCodeReaderFactory codeReaderFactory= SavedCodeReaderFactory.getInstance();
		IASTTranslationUnit ast= lang.getASTTranslationUnit(codeReader, scanInfo, codeReaderFactory, null, options, ParserUtil.getParserLogService());
		if (pm.isCanceled()) {
			return null;
		}
		return ast;
	}
	
	public boolean needToUpdate(IIndexFileLocation location, int confighash) throws CoreException {
		if (super.needToUpdate(location, confighash)) {
			Object required= filePathsToParse.get(location);
			if (required == null) {
				required= MISSING;
				filePathsToParse.put(location, required);
			}
			else if (confighash != 0 && required == REQUIRED_IF_CONFIG_CHANGED) {
				IIndexFile file= fIndex.getFile(location);
				if (file != null) {
					int oldConfig= file.getScannerConfigurationHashcode();
					if (oldConfig == 0 || oldConfig == confighash) {
						required= SKIP;
						updateInfo(0, 0, -1);
					}
					else {
						required= REQUIRED;
					}
					filePathsToParse.put(location, required);
				}
			}
			return required != SKIP;
		}
		return false;
	}

	protected boolean postAddToIndex(IIndexFileLocation location, IIndexFile file)
			throws CoreException {
		Object required= filePathsToParse.get(location);
		filePathsToParse.put(location, SKIP);
		return required == REQUIRED;
	}
}
