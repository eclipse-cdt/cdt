/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.cdt.internal.core.SafeStringInterner;



public abstract class ACSettingEntry implements ICSettingEntry {
	int fFlags;
	String fName;

	ACSettingEntry(String name, int flags){
		fName = SafeStringInterner.safeIntern(name);
		fFlags = flags;
	}

	@Override
	public boolean isBuiltIn() {
		return checkFlags(BUILTIN);
	}

	@Override
	public boolean isReadOnly() {
		return checkFlags(READONLY);
	}

	protected boolean checkFlags(int flags){
		return (fFlags & flags) == flags;
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public String getValue() {
		//name and value differ only for macro entry and have the same contents
		//for all other entries
		return fName;
	}

	@Override
	public boolean isResolved() {
		return checkFlags(RESOLVED);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ACSettingEntry other = (ACSettingEntry) obj;
		if (fFlags != other.fFlags)
			return false;
		if (fName == null) {
			if (other.fName != null)
				return false;
		} else if (!fName.equals(other.fName))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fFlags;
		result = prime * result + ((fName == null) ? 0 : fName.hashCode());
		return result;
	}

	@Override
	public int getFlags() {
		return fFlags;
	}

	@Override
	public boolean equalsByContents(ICSettingEntry entry) {
		return equalsByName(entry);
	}

	protected int getByNameMatchFlags(){
		return (fFlags & (~ (BUILTIN | READONLY | RESOLVED)));
	}

	@Override
	public final boolean equalsByName(ICSettingEntry entry) {
		if(entry == this)
			return true;

		if(!(entry instanceof ACSettingEntry))
			return false;

		ACSettingEntry e = (ACSettingEntry)entry;

		if(getKind() != e.getKind())
			return false;

		if(getByNameMatchFlags()
				!= e.getByNameMatchFlags())
			return false;

		if(!fName.equals(e.fName))
			return false;

		return true;
	}

	public final int codeForNameKey(){
		return getKind() + getByNameMatchFlags() + fName.hashCode();
	}

	public int codeForContentsKey(){
		return codeForNameKey();
	}

	@Override
	public final String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append('[').append(LanguageSettingEntriesSerializer.kindToString(getKind())).append(']').append(' ');
		buf.append(contentsToString());
		buf.append(" ; flags: ").append(LanguageSettingEntriesSerializer.composeFlagsString(getFlags())); //$NON-NLS-1$
		return buf.toString();
	}

	protected abstract String contentsToString();

}
