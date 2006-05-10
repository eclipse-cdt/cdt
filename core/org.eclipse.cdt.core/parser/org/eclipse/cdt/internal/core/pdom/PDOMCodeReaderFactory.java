/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMInclude;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacro;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacroParameter;
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
	
	private static final char[] EMPTY_CHARS = new char[0];
	
	public PDOMCodeReaderFactory(PDOM pdom) {
		this.pdom = pdom;
	}

	public PDOMCodeReaderFactory(PDOM pdom, IWorkingCopy workingCopy) {
		this(pdom);
		workingCopies.add(workingCopy);
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
		IString filename = file.getFileName();
		if (visited.contains(filename))
			return;
		visited.add(filename);
		
		// Follow the includes
		PDOMInclude include = file.getFirstInclude();
		while (include != null) {
			fillMacros(include.getIncludes(), scanner, visited);
			include = include.getNextInIncludes();
		}
		
		// Add in my macros now
		PDOMMacro macro = file.getFirstMacro();
		while (macro != null) {
			char[] name = macro.getName().getChars();
			char[] expansion = macro.getExpansion().getChars();
			
			PDOMMacroParameter param = macro.getFirstParameter();
			if (param != null) {
				List paramList = new ArrayList();
				while (param != null) {
					paramList.add(param.getName().getChars());
					param = param.getNextParameter();
				}
				char[][] params = (char[][])paramList.toArray(new char[paramList.size()][]);
				scanner.addDefinition(name, params, expansion);
			} else
				scanner.addDefinition(name, expansion);
			macro = macro.getNextMacro();
		}
	}
	
	public CodeReader createCodeReaderForInclusion(IScanner scanner, String path) {
		// Don't parse inclusion if it is already captured
		try {
			try {
				File file = new File(path);
				if (!file.exists())
					return null;
				path = new File(path).getCanonicalPath();
			} catch (IOException e) {
				// ignore and use the path we were passed in
			}
			PDOMFile file = pdom.getFile(path);
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

	public ICodeReaderCache getCodeReaderCache() {
		// No need for cache here
		return null;
	}

}
