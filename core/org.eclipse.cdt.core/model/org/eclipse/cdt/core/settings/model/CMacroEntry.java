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
package org.eclipse.cdt.core.settings.model;



public final class CMacroEntry extends ACLanguageSettingEntry implements ICMacroEntry{
	private String fValue;

	public CMacroEntry(String name, String value, int flags) {
		super(name, flags);
		fValue = value;
		if(fValue == null)
			fValue = "";	//$NON-NLS-1$
	}

	public String getValue() {
		return fValue;
	}

	public final int getKind() {
		return MACRO;
	}

	public boolean equals(Object other) {
		if(!super.equals(other))
			return false;
		return fValue.equals(((CMacroEntry)other).fValue);
	}

	public int hashCode() {
		return super.hashCode() + fValue.hashCode();
	}

	public boolean equalsByContents(ICSettingEntry entry) {
		if(!super.equalsByContents(entry))
			return false;
		
		return fValue.equals(((CMacroEntry)entry).fValue);
	}
	
	
}
