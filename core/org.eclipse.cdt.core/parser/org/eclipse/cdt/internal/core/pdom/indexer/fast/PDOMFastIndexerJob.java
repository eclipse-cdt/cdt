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

package org.eclipse.cdt.internal.core.pdom.indexer.fast;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
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
abstract class PDOMFastIndexerJob extends PDOMIndexerTask implements IPDOMIndexerTask {

	protected final PDOMFastIndexer indexer;
	protected IWritableIndex index;
	protected IndexBasedCodeReaderFactory codeReaderFactory;

	public PDOMFastIndexerJob(PDOMFastIndexer indexer) throws CoreException {
		this.indexer = indexer;
	}
	
	protected void setupIndexAndReaderFactory() throws CoreException {
		this.index= ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(indexer.getProject());
		this.codeReaderFactory = new IndexBasedCodeReaderFactory(index);
	}		

	protected void registerTUsInReaderFactory(Collection files) throws CoreException {
		for (Iterator iter = files.iterator(); iter.hasNext();) {
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			FileInfo info= codeReaderFactory.createFileInfo(tu);
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

		HashSet paths= new HashSet();
		paths.add(path.toOSString());
		codeReaderFactory.setPathCollector(paths);
		index.acquireReadLock();
		try {
			// get the AST in a "Fast" way
			IASTTranslationUnit ast= language.getASTTranslationUnit(codeReader, scanner, codeReaderFactory, index);
			if (pm.isCanceled()) {
				return;
			}
			// Clear the macros
			codeReaderFactory.clearMacroAttachements();
				
			// Add the new symbols
			addSymbols(paths, ast, pm);
		}
		finally {
			index.releaseReadLock();
			codeReaderFactory.setPathCollector(null);
		}
	}

	protected void addSymbols(HashSet paths, IASTTranslationUnit ast, IProgressMonitor pm) throws InterruptedException, CoreException {
		// Add in the includes
		final LinkedHashMap symbolMap= new LinkedHashMap();
		String[] orderedPaths= extractSymbols(ast, paths, symbolMap);
		
		for (int i=0; i<orderedPaths.length; i++) {
			if (pm.isCanceled()) {
				return;
			}
			String path= orderedPaths[i];
			FileInfo info= codeReaderFactory.createFileInfo(path);
		
			// file is requested or is not yet indexed.
			if (info.isRequested() || info.fFile == null) {
				prepareIndexInsertion(path, symbolMap);
			}
			else {
				if (fTrace) {
					System.out.println("Indexer: skipping " + path); //$NON-NLS-1$
				}
				orderedPaths[i]= null;
			}
		}

		boolean isFirstRequest= true;
		boolean isFirstAddition= true;
		index.acquireWriteLock(1);
		try {
			for (int i=0; i<orderedPaths.length; i++) {
				if (pm.isCanceled()) 
					return;
				
				String path = orderedPaths[i];
				if (path != null) {
					FileInfo info= codeReaderFactory.createFileInfo(path);
					if (info.isRequested()) {
						info.setRequested(false);

						if (isFirstRequest) 
							isFirstRequest= false;
						else 
							fTotalSourcesEstimate--;
					}
					if (fTrace) {
						System.out.println("Indexer: adding " + path); //$NON-NLS-1$
					}
					info.fFile= addToIndex(index, path, symbolMap);

					if (isFirstAddition) 
						isFirstAddition= false;
					else
						fCompletedHeaders++;
				}
			}
		} finally {
			index.releaseWriteLock(1);
		}
		fCompletedSources++;
	}


	protected void parseTUs(List sources, List headers, IProgressMonitor monitor) throws CoreException, InterruptedException {
		// sources first
		Iterator iter;
		for (iter= sources.iterator(); iter.hasNext();) {
			if (monitor.isCanceled()) 
				return;
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			parseTU(tu, monitor);
		}

		// headers with context
		for (iter= headers.iterator(); iter.hasNext();) {
			if (monitor.isCanceled()) 
				return;
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			FileInfo info= codeReaderFactory.createFileInfo(tu);
			// check if header was handled while parsing a source
			if (!info.isRequested()) {
				iter.remove();
			}
			else if (info.fFile != null) {
				ITranslationUnit context= findContext(index, info.fFile.getLocation());
				if (context != null) {
					parseTU(context, monitor);
				}
			}
		}

		// headers without context
		if (getIndexAllFiles()) {
			for (iter= headers.iterator(); iter.hasNext();) {
				if (monitor.isCanceled()) 
					return;
				ITranslationUnit tu = (ITranslationUnit) iter.next();
				FileInfo info= codeReaderFactory.createFileInfo(tu);
				// check if header was handled while parsing a source
				if (!info.isRequested()) {
					iter.remove();
				}
				else {
					parseTU(tu, monitor);
				}
			}
		}
	}
}
