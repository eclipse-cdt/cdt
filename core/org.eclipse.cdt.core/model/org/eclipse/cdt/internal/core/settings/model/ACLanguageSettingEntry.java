/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;


public abstract class ACLanguageSettingEntry implements ICLanguageSettingEntry {
	int fFlags;
	String fName;
	
	public ACLanguageSettingEntry(String name, int flags){
		fName = name;
		fFlags = flags;
	}

	public boolean isBuiltIn() {
		return checkFlags(BUILTIN);
	}

	public boolean isReadOnly() {
		return checkFlags(READONLY);
	}
	
	protected boolean checkFlags(int flags){
		return (fFlags & flags) == flags;
	}
	
	public String getName() {
		return fName;
	}

	public String getValue() {
		//name and value differ only for macro entry and have the same contents 
		//for all other entries
		return fName;
	}

	public boolean isResolved() {
		return checkFlags(RESOLVED);
	}
	
	public boolean equals(Object other){
		if(other == this)
			return true;
		
		if(!(other instanceof ACLanguageSettingEntry))
			return false;
		
		ACLanguageSettingEntry e = (ACLanguageSettingEntry)other;
		
		if(getKind() != e.getKind())
			return false;
		
		if(fFlags != e.fFlags)
			return false;
		
		if(!fName.equals(e.fName))
			return false;
		
		return true;
	}
	
	public int hashCode(){
		return getKind() + fFlags + fName.hashCode(); 
	}

	public int getFlags() {
		return fFlags;
	}

	public boolean equalsByContents(ICLanguageSettingEntry entry) {
		return equalsByName(entry);
	}
	
	protected int getByNameMatchFlags(){
		return (fFlags & (~ (BUILTIN | READONLY)));
	}

	public final boolean equalsByName(ICLanguageSettingEntry entry) {
		if(entry == this)
			return true;
		
		if(!(entry instanceof ACLanguageSettingEntry))
			return false;
		
		ACLanguageSettingEntry e = (ACLanguageSettingEntry)entry;
		
		if(getKind() != e.getKind())
			return false;
		
		if(getByNameMatchFlags()
				!= e.getByNameMatchFlags())
			return false;
		
		if(!fName.equals(e.fName))
			return false;
		
		return true;
	}
	
	public int codeForNameKey(){
		return getKind() + getByNameMatchFlags() + fName.hashCode(); 
	}
	
}
