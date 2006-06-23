/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

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
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMInclude;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacro;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCodeReaderFactory implements ICodeReaderFactory {

	private final PDOM pdom;
	
	private List workingCopies = new ArrayList(1);
	private Map fileCache = new HashMap(); // filename, pdomFile
	private Map macroCache = new HashMap();// record, list of IMacros
	
	private List usedMacros = new ArrayList();
	
	private static final char[] EMPTY_CHARS = new char[0];
	
	public PDOMCodeReaderFactory(PDOM pdom) {
		this.pdom = pdom;
	}

	public PDOMCodeReaderFactory(PDOM pdom, IWorkingCopy workingCopy) {
		this(pdom);
		workingCopies.add(workingCopy);
	}
	
	public PDOMFile getCachedFile(String filename) throws CoreException {
		PDOMFile file = (PDOMFile)fileCache.get(filename);
		if (file == null) {
			file = pdom.addFile(filename);
			fileCache.put(filename, file);
		}
		return file;
	}
	
	public int getUniqueIdentifier() {
		return 0;
	}

	public CodeReader createCodeReaderForTranslationUnit(String path) {
		return ParserUtil.createReader(path,
				workingCopies != null ? workingCopies.iterator() : null);
	}

	public CodeReader createCodeReaderForTranslationUnit(ITranslationUnit tu) {
		return new CodeReader(tu.getResource().getLocation().toOSString(), tu.getContents());
	}
	
	private void fillMacros(PDOMFile file, IScanner scanner, Set visited) throws CoreException {
		Integer record = new Integer(file.getRecord());
		if (visited.contains(record))
			return;
		visited.add(record);

		// Follow the includes
		PDOMInclude include = file.getFirstInclude();
		while (include != null) {
			fillMacros(include.getIncludes(), scanner, visited);
			include = include.getNextInIncludes();
		}
		
		// Add in my macros now
		IMacro[] macros = (IMacro[])macroCache.get(record);
		if (macros == null) {
			List macroList = new ArrayList();
			PDOMMacro macro = file.getFirstMacro();
			while (macro != null) {
				macroList.add(macro.getMacro());
				macro = macro.getNextMacro();
			}
			macros = (IMacro[])macroList.toArray(new IMacro[macroList.size()]);
			macroCache.put(record, macros);
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
			PDOMFile file = (PDOMFile)fileCache.get(path);
			if (file == null) {
				file = pdom.getFile(path);
				if (file != null)
					fileCache.put(path, file);
			}
			if (file != null) {
				// Already got things from here,
				// add the macros to the scanner
				fillMacros(file, scanner, new HashSet());
				return new CodeReader(path, EMPTY_CHARS);
			}
		} catch (CoreException e) {
			CCorePlugin.log(new CoreException(new Status(IStatus.ERROR,
					CCorePlugin.PLUGIN_ID, 0, "PDOM Exception", e)));
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

}
