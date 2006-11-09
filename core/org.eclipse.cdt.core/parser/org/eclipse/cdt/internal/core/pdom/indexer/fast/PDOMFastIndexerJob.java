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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory.FileInfo;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * @author Doug Schaefer
 *
 */
abstract class PDOMFastIndexerJob extends PDOMIndexerTask implements IPDOMIndexerTask {

	protected final PDOMFastIndexer indexer;
	protected IWritableIndex index;
	protected IndexBasedCodeReaderFactory codeReaderFactory;
	private boolean fTrace= false;

	public PDOMFastIndexerJob(PDOMFastIndexer indexer) throws CoreException {
		this.indexer = indexer;
		String trace = Platform.getDebugOption(CCorePlugin.PLUGIN_ID + "/debug/indexer"); //$NON-NLS-1$
		if (trace != null && trace.equalsIgnoreCase("true")) { //$NON-NLS-1$
			fTrace= true;
		}
	}
	
	protected void setupIndexAndReaderFactory() throws CoreException {
		this.index= ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(indexer.getProject());
		this.codeReaderFactory = new IndexBasedCodeReaderFactory(index);
	}		

	protected void registerTUsInReaderFactory(Collection tus) throws CoreException {
		for (Iterator iter = tus.iterator(); iter.hasNext();) {
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			FileInfo info= codeReaderFactory.createFileInfo(tu);
			info.fNeedToIndex= true;
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

		LinkedHashSet paths= new LinkedHashSet();
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

	protected void addSymbols(Collection paths, IASTTranslationUnit ast, IProgressMonitor pm) throws InterruptedException, CoreException {
		// Add in the includes
		final HashMap symbolMap= new HashMap();
		
		// includes
		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
		for (int i = 0; i < includes.length; ++i) {
			IASTPreprocessorIncludeStatement include = includes[i];
			IASTFileLocation sourceLoc = include.getFileLocation();
			String path= sourceLoc != null ? sourceLoc.getFileName() : ast.getFilePath(); // command-line includes
			addToMap(symbolMap, 0, path, include);
		}
	
		// macros
		IASTPreprocessorMacroDefinition[] macros = ast.getMacroDefinitions();
		for (int i = 0; i < macros.length; ++i) {
			IASTPreprocessorMacroDefinition macro = macros[i];
			IASTFileLocation sourceLoc = macro.getFileLocation();
			if (sourceLoc != null) { // skip built-ins and command line macros
				String path = sourceLoc.getFileName();
				addToMap(symbolMap, 1, path, macro);
			}
		}
			
		// names
		ast.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
				shouldVisitDeclarations = true;
			}
			public int visit(IASTName name) {
				try {
					IASTFileLocation nameLoc = name.getFileLocation();
					if (nameLoc != null) {
						addToMap(symbolMap, 2, nameLoc.getFileName(), name);
					}
					return PROCESS_CONTINUE;
				} catch (Throwable e) {
					CCorePlugin.log(e);
					return ++fErrorCount > MAX_ERRORS ? PROCESS_ABORT : PROCESS_CONTINUE;
				}
			}
		});

		for (Iterator iter = symbolMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			if (pm.isCanceled()) {
				return;
			}
			String path= (String) entry.getKey();
			FileInfo info= codeReaderFactory.createFileInfo(path);
			if (!info.fNeedToIndex && info.fFile != null) {
				if (fTrace) {
					System.out.println("Indexer: skipping " + path); //$NON-NLS-1$
				}
				iter.remove();
			}
			else {
				// resolve the names
				ArrayList names= ((ArrayList[]) entry.getValue())[2];
				for (int i=0; i<names.size(); i++) {
					((IASTName) names.get(i)).resolveBinding();
				}
			}				
		}

		index.acquireWriteLock(1);
		try {
			for (Iterator iter = paths.iterator(); iter.hasNext();) {
				String path = (String) iter.next();
				FileInfo info= codeReaderFactory.createFileInfo(path);
				if (!info.fNeedToIndex) {
					fTotalTasks++;
				}
				info.fNeedToIndex= false;
				if (fTrace) {
					System.out.println("Indexer: adding " + path); //$NON-NLS-1$
				}
				addToIndex(path, info, (ArrayList[]) symbolMap.get(path));
				fCompletedTasks++;
				if (pm.isCanceled()) {
					return;
				}
			}
		} finally {
			index.releaseWriteLock(1);
		}
	}

	private void addToMap(HashMap map, int idx, String path, Object thing) {
		List[] lists= (List[]) map.get(path);
		if (lists == null) {
			lists= new ArrayList[]{new ArrayList(), new ArrayList(), new ArrayList()};
			map.put(path, lists);
		}
		lists[idx].add(thing);
	}		

	private void addToIndex(String location, FileInfo info, ArrayList[] lists) throws CoreException {
		// Remove the old symbols in the tu
		Path path= new Path(location);
		IIndexFragmentFile file= (IIndexFragmentFile) info.fFile;
		if (file != null) {
			index.clearFile(file);
		}
		else {
			file= index.addFile(path);
			info.fFile= file;
		}
		file.setTimestamp(path.toFile().lastModified());

		if (lists != null) {
			// includes
			ArrayList list= lists[0];
			for (int i = 0; i < list.size(); i++) {
				IASTPreprocessorIncludeStatement include= (IASTPreprocessorIncludeStatement) list.get(i);
				IIndexFragmentFile destFile= createIndexFile(include.getPath());
				index.addInclude(file, destFile, include);
			}

			// macros
			list= lists[1];
			for (int i = 0; i < list.size(); i++) {
				index.addMacro(file, (IASTPreprocessorMacroDefinition) list.get(i));
			}

			// symbols
			list= lists[2];
			for (int i = 0; i < list.size(); i++) {
				index.addName(file, (IASTName) list.get(i));
			}	
		}
	}

	private IIndexFragmentFile createIndexFile(String path) throws CoreException {
		FileInfo info= codeReaderFactory.createFileInfo(path);
		if (info.fFile == null) {
			info.fFile= index.addFile(new Path(path));
		}
		return (IIndexFragmentFile) info.fFile;
	}
	
	protected void parseTUs(List translationUnits, IProgressMonitor monitor) throws CoreException, InterruptedException {
		// sources first
		Iterator i = translationUnits.iterator();
		while (i.hasNext()) {
			if (monitor.isCanceled()) 
				return;
			ITranslationUnit tu = (ITranslationUnit)i.next();
			if (tu.isSourceUnit()) {
				parseTU(tu, monitor);
				i.remove();
				fCompletedTasks++;
			}
		}

		// headers with context
		i = translationUnits.iterator();
		while (i.hasNext()) {
			if (monitor.isCanceled())
				return;
			ITranslationUnit tu = (ITranslationUnit)i.next();
			FileInfo info= codeReaderFactory.createFileInfo(tu);
			if (!info.fNeedToIndex) {
				i.remove();
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
			i = translationUnits.iterator();
			while (i.hasNext()) {
				if (monitor.isCanceled())
					return;
				ITranslationUnit tu = (ITranslationUnit)i.next();
				FileInfo info= codeReaderFactory.createFileInfo(tu);
				if (!info.fNeedToIndex) {
					i.remove();
				}
				else {
					parseTU(tu, monitor);
				}
			}
		}
	}
}
