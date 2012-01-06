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

import org.eclipse.cdt.internal.core.SafeStringInterner;



public final class CMacroEntry extends ACSettingEntry implements ICMacroEntry{
	private String fValue;

	public CMacroEntry(String name, String value, int flags) {
		super(name, flags);
		fValue = SafeStringInterner.safeIntern(value);
		if(fValue == null)
			fValue = "";	//$NON-NLS-1$
	}

	@Override
	public String getValue() {
		return fValue;
	}

	@Override
	public final int getKind() {
		return MACRO;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CMacroEntry other = (CMacroEntry) obj;
		if (fValue == null) {
			if (other.fValue != null)
				return false;
		} else if (!fValue.equals(other.fValue))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fValue == null) ? 0 : fValue.hashCode());
		return result;
	}

	@Override
	public boolean equalsByContents(ICSettingEntry entry) {
		if(!super.equalsByContents(entry))
			return false;

		return fValue.equals(((CMacroEntry)entry).fValue);
	}

	@Override
	protected String contentsToString() {
		return new StringBuffer().append(fName).append('=').append(fValue).toString();
	}

}
