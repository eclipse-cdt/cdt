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

/**
 * Representation of a declared file type.
 */
public class CFileType implements ICFileType {

	private String	fId;
	private String	fName;
	private int		fType;

	public CFileType(String id, String name, int type) {
		fId		= id;
		fName	= name;
		fType	= type;
	}
	
	public String getId() {
		return fId;
	}

	public String getName() {
		return fName;
	}

	public int getType() {
		return fType;
	}

	public boolean isSource() {
		return (ICFileType.TYPE_SOURCE == fType);
	}

	public boolean isHeader() {
		return (ICFileType.TYPE_HEADER == fType);
	}

	public boolean isTranslationUnit() {
		return (isSource() || isHeader());
	}
}
