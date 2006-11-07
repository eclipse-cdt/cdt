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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Doug Schaefer
 *
 */
abstract class PDOMFullIndexerJob extends PDOMIndexerTask implements IPDOMIndexerTask {

	protected final PDOMFullIndexer indexer;
	protected IWritableIndex index= null;
	private Set filePathsToParse= null;
	protected volatile int fFilesToIndex= 0;

	public PDOMFullIndexerJob(PDOMFullIndexer indexer) throws CoreException {
		this.indexer = indexer;
	}

	public IPDOMIndexer getIndexer() {
		return indexer;
	}
	
	protected void setupIndexAndReaderFactory() throws CoreException {
		this.index = ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(indexer.getProject());
	}
	
	protected void registerTUsInReaderFactory(Collection tus) throws CoreException {
		filePathsToParse= new HashSet();
		
		for (Iterator iter = tus.iterator(); iter.hasNext();) {
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			filePathsToParse.add(tu.getLocation().toOSString());
		}
	}
	
	protected void parseTUs(Collection translationUnits, IProgressMonitor monitor) throws CoreException, InterruptedException {
		// sources first
		Iterator i = translationUnits.iterator();
		while (i.hasNext()) {
			if (monitor.isCanceled()) 
				return;
			ITranslationUnit tu = (ITranslationUnit)i.next();
			String path = tu.getLocation().toOSString();
			if (!filePathsToParse.contains(path)) {
				i.remove();
			}
			else if (tu.isSourceUnit()) {
				parseTU(tu);
				i.remove();
			}
		}

		// headers with context
		i = translationUnits.iterator();
		while (i.hasNext()) {
			if (monitor.isCanceled())
				return;
			ITranslationUnit tu = (ITranslationUnit)i.next();
			String path = tu.getLocation().toOSString();
			if (!filePathsToParse.contains(path)) {
				i.remove();
			}
			else {
				ITranslationUnit context= findContext(index, path);
				if (context != null) {
					parseTU(context);
				}
			}
		}

		// headers without context
		if (getIndexAllFiles()) {
			i = translationUnits.iterator();
			while (i.hasNext()) {
				ITranslationUnit tu = (ITranslationUnit)i.next();
				String path = tu.getLocation().toOSString();
				if (!filePathsToParse.contains(path)) {
					i.remove();
				}
				else {
					parseTU(tu);
				}
			}
		}
	}

	protected void doParseTU(ITranslationUnit tu) throws CoreException, InterruptedException {
		IPath path = tu.getLocation();
		if (path == null) {
			return;
		}
		int options= 0;
		if (!getIndexAllFiles()) {
			options |= ITranslationUnit.AST_SKIP_IF_NO_BUILD_INFO;
		}
		IASTTranslationUnit ast= tu.getAST(null, options);
		if (ast == null)
			return;
		System.out.println(path.toOSString());
		index.acquireWriteLock(0);
		
		try {
			// Remove the old symbols in the tu
			IIndexFragmentFile file = (IIndexFragmentFile) index.getFile(path);
			if (file != null)
				index.clearFile(file);

			// Clear out the symbols in the includes
			IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
			for (int i = 0; i < includes.length; ++i) {
				String incname = includes[i].getPath();
				IIndexFragmentFile incfile = (IIndexFragmentFile) index.getFile(new Path(incname));
				if (incfile != null) {
					index.clearFile(incfile);
				}
			}
			
			addSymbols(ast);
		} finally {
			index.releaseWriteLock(0);
		}
	}
	
	protected void addSymbols(IASTTranslationUnit ast) throws InterruptedException, CoreException {
		// Add in the includes
		final LinkedHashMap symbolMap= new LinkedHashMap(); // makes bugs reproducible
		
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

		for (Iterator iter = symbolMap.values().iterator(); iter.hasNext();) {
			// resolve the names
			ArrayList names= ((ArrayList[]) iter.next())[2];
			for (int i=0; i<names.size(); i++) {
				((IASTName) names.get(i)).resolveBinding();
			}
		}

		for (Iterator iter = symbolMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			String path= (String) entry.getKey();
			addToIndex(path, (ArrayList[]) entry.getValue());
		}
	}

	private void addToMap(HashMap map, int idx, String path, Object thing) {
		if (filePathsToParse.contains(path)) {
			List[] lists= (List[]) map.get(path);
			if (lists == null) {
				lists= new ArrayList[]{new ArrayList(), new ArrayList(), new ArrayList()};
				map.put(path, lists);
			}
			lists[idx].add(thing);
		}
	}		

	private void addToIndex(String location, ArrayList[] lists) throws CoreException {
		if (!filePathsToParse.remove(location)) {
			return;
		}
		fFilesToIndex--;

		// Remove the old symbols in the tu
		Path path= new Path(location);
		IIndexFragmentFile file= (IIndexFragmentFile) index.getFile(new Path(location));
		if (file != null) {
			index.clearFile(file);
		}
		else {
			file= index.addFile(path);
		}
		file.setTimestamp(path.toFile().lastModified());
		
		// includes
		ArrayList list= lists[0];
		for (int i = 0; i < list.size(); i++) {
			IASTPreprocessorIncludeStatement include= (IASTPreprocessorIncludeStatement) list.get(i);
			IIndexFragmentFile destFile= index.addFile(new Path(include.getPath()));
			index.addInclude(file, destFile);
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
	
	final public int getFilesToIndexCount() {
		return fFilesToIndex;
	}
}
