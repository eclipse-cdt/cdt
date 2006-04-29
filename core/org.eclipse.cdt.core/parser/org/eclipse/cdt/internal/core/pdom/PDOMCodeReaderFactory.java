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
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.pdom.db.IString;
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
	private Set skippedHeaders = new HashSet();
	
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

	public Set getSkippedHeaders() {
		return skippedHeaders;
	}
	
	public CodeReader createCodeReaderForTranslationUnit(String path) {
		return ParserUtil.createReader(path,
				workingCopies != null ? workingCopies.iterator() : null);
	}

	public CodeReader createCodeReaderForTranslationUnit(ITranslationUnit tu) {
		return new CodeReader(tu.getResource().getLocation().toOSString(), tu.getContents());
	}
	
	private void fillMacros(PDOMFile file, StringBuffer buffer, Set visited) throws CoreException {
		IString filename = file.getFileName();
		if (visited.contains(filename))
			return;
		visited.add(filename);
		
		// Follow the includes
		PDOMInclude include = file.getFirstInclude();
		while (include != null) {
			fillMacros(include.getIncludes(), buffer, visited);
			include = include.getNextInIncludes();
		}
		
		// Add in my macros now
		PDOMMacro macro = file.getFirstMacro();
		while (macro != null) {
			buffer.append("#define "); //$NON-NLS-1$
			buffer.append(macro.getName().getChars());
			buffer.append(' ');
			buffer.append(macro.getExpansion().getChars());
			buffer.append('\n');
			macro = macro.getNextMacro();
		}
	}
	
	public CodeReader createCodeReaderForInclusion(String path) {
		// Don't parse inclusion if it is already captured
		try {
			try {
				path = new File(path).getCanonicalPath();
			} catch (IOException e) {
				// ignore and use the path we were passed in
			}
			PDOMFile file = pdom.getFile(path);
			if (file != null) {
				// Already got things from here, pass in a magic
				// buffer with the macros in it
				skippedHeaders.add(path);
				StringBuffer buffer = new StringBuffer();
				fillMacros(file, buffer, new HashSet());
				int length = buffer.length();
				char[] chars = new char[length];
				buffer.getChars(0, length, chars, 0);
				return new CodeReader(path, chars);
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
