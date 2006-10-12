/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * @author Doug Schaefer
 *
 */
public class IndexBasedCodeReaderFactory implements ICodeReaderFactory {

	private final IIndex index;
	
	private Map fileCache = new HashMap(); // filename, pdomFile
	private Map macroCache = new HashMap();// record, list of IMacros
	private List usedMacros = new ArrayList();
	
	private static final char[] EMPTY_CHARS = new char[0];
	
	public IndexBasedCodeReaderFactory(IIndex index) {
		this.index = index;
	}
	
	public int getUniqueIdentifier() {
		return 0;
	}

	public CodeReader createCodeReaderForTranslationUnit(String path) {
		return ParserUtil.createReader(path, null);
	}
	
	private void fillMacros(IIndexFragmentFile file, IScanner scanner, Set visited) throws CoreException {
		Object key= file.getLocation();	// mstodo revisit, the pdom-id was faster but cannot be used in indexes.
		if (!visited.add(key)) {
			return;
		}

		// Follow the includes
		IIndexInclude[] includeDirectives= file.getIncludes();
		for (int i = 0; i < includeDirectives.length; i++) {
			IIndexFragmentFile includedFile= (IIndexFragmentFile) index.resolveInclude(includeDirectives[i]);
			if (includedFile != null) {
				fillMacros(includedFile, scanner, visited);
			}
			// mstodo revisit, what if includedFile == null (problem with index??)
		}
		
		// Add in my macros now
		IMacro[] macros = (IMacro[]) macroCache.get(key);
		if (macros == null) {
			macros= file.getMacros();
			macroCache.put(key, macros);
		}

		for (int i = 0; i < macros.length; ++i)
			scanner.addDefinition(macros[i]);

		// record the macros we used.
		usedMacros.add(macros);
	}
	
	public CodeReader createCodeReaderForInclusion(IScanner scanner, String path) {
		// Don't parse inclusion if it is already captured
		try {
			try {
				File file = new File(path);
				if (!file.exists())
					return null;
				path = file.getCanonicalPath();
			} catch (IOException e) {
				// ignore and use the path we were passed in
			}
			IIndexFragmentFile file = getCachedFile(path);
			if (file == null) {
				file = (IIndexFragmentFile) index.getFile(new Path(path));
				if (file != null) {
					addFileToCache(path, file);
				}
			}
			if (file != null) {
				// Already got things from here,
				// add the macros to the scanner
				fillMacros(file, scanner, new HashSet());
				return new CodeReader(path, EMPTY_CHARS);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e); 
		}
		
		return ParserUtil.createReader(path, null);
	}
	
	public void clearMacros() {
		Iterator i = usedMacros.iterator();
		while (i.hasNext()) {
			IMacro[] macros = (IMacro[])i.next();
			for (int j = 0; j < macros.length; ++j) {
				if (macros[j] instanceof ObjectStyleMacro) {
					((ObjectStyleMacro)macros[j]).attachment = null;
				}
			}
		}
		usedMacros.clear();
	}

	public ICodeReaderCache getCodeReaderCache() {
		// No need for cache here
		return null;
	}

	protected IIndexFragmentFile getCachedFile(String filename) throws CoreException {
		IIndexFragmentFile file = (IIndexFragmentFile) fileCache.get(filename);
		if (file == null) {
			file = (IIndexFragmentFile) index.getFile(new Path(filename));
			if (file != null) {
				addFileToCache(filename, file);
			}
		}
		return file;
	}
	
	protected void addFileToCache(String filename, IIndexFile file) {
		fileCache.put(filename, file);
	}

	public IIndexFragmentFile createCachedFile(IWritableIndex index, String location) throws CoreException {
		assert this.index == index;
		
		IIndexFragmentFile file= getCachedFile(location);
		if (file == null) {
			file= index.addFile(location);
		}
		return file;
	}
}
