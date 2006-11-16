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
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
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

	protected final PDOMFullIndexer indexer;
	protected IWritableIndex index= null;
	private Map filePathsToParse= null;

	public PDOMFullIndexerJob(PDOMFullIndexer indexer) throws CoreException {
		this.indexer = indexer;
	}

	public IPDOMIndexer getIndexer() {
		return indexer;
	}
	
	protected void setupIndexAndReaderFactory() throws CoreException {
		this.index = ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(indexer.getProject());
	}
	
	protected void registerTUsInReaderFactory(Collection sources, Collection headers, 
			boolean requireHeaders) throws CoreException {
		filePathsToParse= new HashMap();
		
		for (Iterator iter = sources.iterator(); iter.hasNext();) {
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			filePathsToParse.put(tu.getLocation().toOSString(), Boolean.TRUE);
		}
		Boolean required= Boolean.valueOf(requireHeaders);
		for (Iterator iter = headers.iterator(); iter.hasNext();) {
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			filePathsToParse.put(tu.getLocation().toOSString(), required);
		}
	}
	
	protected void parseTUs(Collection sources, Collection headers, IProgressMonitor monitor) throws CoreException, InterruptedException {
		// sources first
		Iterator iter;
		for (iter = sources.iterator(); iter.hasNext();) {
			if (monitor.isCanceled()) 
				return;
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			String path = tu.getLocation().toOSString();
			if (filePathsToParse.get(path) != null) {
				parseTU(tu, monitor);
			}
		}

		// headers with context
		for (iter = headers.iterator(); iter.hasNext();) {
			if (monitor.isCanceled()) 
				return;
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			String path = tu.getLocation().toOSString();
			if (filePathsToParse.get(path)==null) {
				iter.remove();
			}
			else {
				ITranslationUnit context= findContext(index, path);
				if (context != null) {
					parseTU(context, monitor);
				}
			}
		}

		// headers without context
		if (getIndexAllFiles()) {
			for (iter = headers.iterator(); iter.hasNext();) {
				if (monitor.isCanceled()) 
					return;
				ITranslationUnit tu = (ITranslationUnit) iter.next();
				String path = tu.getLocation().toOSString();
				if (filePathsToParse.get(path)==null) {
					iter.remove();
				}
				else {
					parseTU(tu, monitor);
				}
			}
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
	
	protected void addSymbols(IASTTranslationUnit ast, IProgressMonitor pm) throws InterruptedException, CoreException {
		// Add in the includes
		final LinkedHashMap symbolMap= new LinkedHashMap(); 
		String[] orderedPaths= extractSymbols(ast, filePathsToParse.keySet(), symbolMap);
		
		for (int i=0; i<orderedPaths.length; i++) {
			if (pm.isCanceled()) {
				return;
			}
			String path= orderedPaths[i];
			prepareIndexInsertion(path, symbolMap);
		}

		boolean isFirstRequest= true;
		boolean isFirstAddition= true;
		index.acquireWriteLock(0);
		try {
			for (int i=0; i<orderedPaths.length; i++) {
				if (pm.isCanceled()) 
					return;
				
				String path = orderedPaths[i];
				Boolean required= (Boolean) filePathsToParse.remove(path);
				if (required != null) {
					if (required.booleanValue()) {
						if (isFirstRequest) 
							isFirstRequest= false;
						else 
							fTotalSourcesEstimate--;
					}

					if (fTrace) 
						System.out.println("Indexer: adding " + path); //$NON-NLS-1$
					
					addToIndex(index, path, symbolMap);
					
					if (isFirstAddition) 
						isFirstAddition= false;
					else
						fCompletedHeaders++;
				}
			}
		} finally {
			index.releaseWriteLock(0);
		}
		fCompletedSources++;
	}
}
