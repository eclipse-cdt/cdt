/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
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

/**
 * Representation in the project model of macro settings entries.
 * As an example, those are supplied by a gcc compiler with option "-D".
 */
public final class CMacroEntry extends ACSettingEntry implements ICMacroEntry {
	private final String fValue;

	/**
	 * This constructor is discouraged to be referenced by clients.
	 *
	 * Instead, use pooled entries with CDataUtil.createCMacroEntry(name, value, flags).
	 *
	 * @param name - name of the macro.
	 * @param value - value of the macro.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 */
	public CMacroEntry(String name, String value, int flags) {
		super(name, flags);
		String val = SafeStringInterner.safeIntern(value);
		fValue = val != null ? val : ""; //$NON-NLS-1$
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
		return new StringBuffer().append(getName()).append('=').append(fValue).toString();
	}

}
