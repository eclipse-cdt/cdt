/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.File;

/** 
 * Represents an entry of the include search path
 */
public final class IncludeSearchPathElement {
	private static final boolean NON_SLASH_SEPARATOR = File.separatorChar != '/';
	public static final String FRAMEWORK_VAR = "__framework__"; //$NON-NLS-1$
	public static final String FILE_VAR = "__header__"; //$NON-NLS-1$

	private final String fPath;
	private final boolean fForQuoteIncludesOnly;
	private final boolean fIsFrameworkDirectory;   
	
	IncludeSearchPathElement(String path, boolean forQuoteIncludesOnly) {
		fPath= path;
		fForQuoteIncludesOnly= forQuoteIncludesOnly;
		
		if (path.indexOf('_') != -1 && path.indexOf(FRAMEWORK_VAR) != -1 && path.indexOf(FILE_VAR) != -1) {
			fIsFrameworkDirectory= true;
		} else {
			fIsFrameworkDirectory= false;
		}
	}
	
	public boolean isForQuoteIncludesOnly() {
		return fForQuoteIncludesOnly;
	}

	public String getLocation(String includeDirective) {
		if (fIsFrameworkDirectory) {
			int firstSep = firstSeparator(includeDirective);
			if (firstSep < 1) {
				return null;
			}
			String framework = includeDirective.substring(0, firstSep);
			String file= includeDirective.substring(firstSep + 1);
			if (file.length() == 0)
				return null;
			
			StringBuilder buf= new StringBuilder(fPath);
			replace(buf, FRAMEWORK_VAR, framework);
			replace(buf, FILE_VAR, file);
			return ScannerUtility.reconcilePath(buf.toString());
		}
		return ScannerUtility.createReconciledPath(fPath, includeDirective);
	}

	private int firstSeparator(String path) {
		int firstSep= path.indexOf('/');
		if (NON_SLASH_SEPARATOR) {
			firstSep= Math.max(firstSep, path.indexOf(File.separatorChar));
		}
		return firstSep;
	}

	private void replace(StringBuilder buf, String find, final String replace) {
		int idx= buf.indexOf(find);
		if (idx >= 0) {
			buf.replace(idx, idx + find.length(), replace);
		}
	}

	/**
	 * For debugging only.
	 */
	@Override
	public String toString() {
		return fPath;
	}
}