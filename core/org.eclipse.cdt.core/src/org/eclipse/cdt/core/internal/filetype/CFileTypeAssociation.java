/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.internal.filetype;

import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.internal.core.index.StringMatcher;

/**
 * Representation of a declared file type association.
 */
public class CFileTypeAssociation implements ICFileTypeAssociation {

	private String			fPattern;
	private ICFileType		fType;
	private StringMatcher 	fMatcher;
	
	public CFileTypeAssociation(String pattern, ICFileType type) {
		fPattern	= pattern;
		fType	 	= type;
		fMatcher	= new StringMatcher(pattern, false, false);
	}

	public String getPattern() {
		return fPattern;
	}

	public ICFileType getType() {
		return fType;
	}
	
	public boolean matches(String fileName) {
		return fMatcher.match(fileName);
	}
}
