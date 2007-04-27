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
public class StandaloneFullIndexerTask extends StandaloneIndexerTask {
	private final static Object REQUIRED= new Object();
	private final static Object MISSING = new Object();
	private final static Object SKIP=     new Object();
	
	private List fChanged = new ArrayList();
	private List fRemoved = new ArrayList();
	private IWritableIndex fIndex = null;
	private Map filePathsToParse = new HashMap/*<IIndexFileLocation, Object>*/();
	private Map fIflCache = new HashMap/*<String, IIndexFileLocation>*/();

	public StandaloneFullIndexerTask(StandaloneFullIndexer indexer, List added,
			List changed, List removed) {
		super(indexer);
		fChanged.addAll(added);
		fChanged.addAll(changed);
		fRemoved.addAll(removed);
		updateInfo(0, 0, fChanged.size() + fRemoved.size());
	}

	public void run(IProgressMonitor monitor) throws IOException {
		long start = System.currentTimeMillis();
		try {
			setupIndex();
			registerTUsInReaderFactory(fChanged);
			
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
					
			Iterator i= fRemoved.iterator();
			while (i.hasNext()) {
				if (monitor.isCanceled())
					return;
				String tu = (String) i.next();
				removeTU(fIndex, getIndexFileLocation(tu), 0);
				if (isValidSourceUnitName(tu)) {
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
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
		traceEnd(start);
	}

	private void setupIndex() throws CoreException {		
		fIndex = fIndexer.getIndex();
		fIndex.resetCacheCounters();
	}

	private void registerTUsInReaderFactory(Collection/*<ITranslationUnit>*/ sources)
			throws IOException, CoreException {
		int removed= 0;
		filePathsToParse= new HashMap/*<IIndexFileLocation, Object>*/();
		for (Iterator iter = sources.iterator(); iter.hasNext();) {
			String sourcePath = (String) iter.next();
			String path = new File(sourcePath).getCanonicalPath();
			IIndexFileLocation location = getIndexFileLocation(path);
			if (updateAll()) {
				filePathsToParse.put(location, REQUIRED);
			}
			else if (updateChangedTimestamps() && isOutdated(location, fIndex.getFile(location))) {
				filePathsToParse.put(location, REQUIRED);
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
			//Standalone indexing stores the absolute paths of files being indexed
			result = new IndexFileLocation(URIUtil.toURI(absolutePath),absolutePath); 
			fIflCache.put(absolutePath, result);
		}
		return result;		
	}

	protected IASTTranslationUnit createAST(AbstractLanguage lang, CodeReader codeReader, IScannerInfo scanInfo, int options, IProgressMonitor pm) throws CoreException {		
		// get the AST in a "Fast" way
		IASTTranslationUnit ast= lang.getASTTranslationUnit(codeReader, scanInfo, ((StandaloneFullIndexer)fIndexer).getCodeReaderFactory(), null, options, fIndexer.getParserLog());
		if (pm.isCanceled()) {
			return null;
		}		
		return ast;
	}

	protected boolean needToUpdate(IIndexFileLocation location, int confighash) throws CoreException {
		if (super.needToUpdate(location, confighash)) {
			Object required= filePathsToParse.get(location);
			if (required == null) {
				required= MISSING;
				filePathsToParse.put(location, required);
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
