/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.internal.filetype;

import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICLanguage;

/**
 * Representation of a declared file type.
 */
public class CFileType implements ICFileType {

	private ICLanguage	fLang;
	private String		fId;
	private String		fName;
	private int			fType;

	public CFileType(String id, ICLanguage language, String name, int type) {
		Argument.check(id);
		Argument.check(language);
		Argument.check(name);
		Argument.check(type, ICFileType.TYPE_UNKNOWN, ICFileType.TYPE_HEADER);
		
		fId		= id;
		fLang	= language;	
		fName	= name;
		fType	= type;
	}
	
	public String getId() {
		return fId;
	}

	public ICLanguage getLanguage() {
		return fLang;
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
	
	public boolean equals(Object object) {
		if (!(object instanceof ICFileType)) {
			return false;
		}

		ICFileType rhs = (ICFileType) object;
		boolean eq = (fType == rhs.getType());

		if (eq) eq = fId.equals(rhs.getId());
		if (eq) eq = fLang.equals(rhs.getLanguage());
		if (eq) eq = fName.equals(rhs.getName());

		return eq;
	}
}
