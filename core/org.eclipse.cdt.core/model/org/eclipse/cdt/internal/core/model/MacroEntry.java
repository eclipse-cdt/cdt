/**********************************************************************
 * Created on Mar 25, 2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.core.runtime.IPath;

public class MacroEntry extends APathEntry implements IMacroEntry {

	String macroName;
	String macroValue;

	public MacroEntry (IPath path, IPath baseRef, String macroName, String macroValue, IPath[] exclusionPatterns, boolean isExported) {
		super(IMacroEntry.CDT_MACRO, null, baseRef, path, exclusionPatterns, isExported);
		if ( macroName == null) {
			throw new IllegalArgumentException("Macro name cannot be null"); //$NON-NLS-1$
		}
		this.macroName = macroName;
		this.macroValue = macroValue == null ? "" : macroValue; //$NON-NLS-1$
	}

	/**
	 * Returns the macro name.
	 * @return String
	 */
	public String getMacroName() {
		return macroName;
	}

	/**
	 * Returns the macro value.
	 * @return String
	 */
	public String getMacroValue() {
		return macroValue;
	}

	public boolean equals(Object obj) {
		if (obj instanceof IMacroEntry) {
			IMacroEntry otherEntry = (IMacroEntry)obj;
			if (!super.equals(otherEntry)) {
				return false;
			}
			if (macroName == null) {
				if (otherEntry.getMacroName() != null) {
					return false;
				}
			} else {
				if (!macroName.equals(otherEntry.getMacroName())) {
					return false;
				}
			}
			if (macroValue == null) {
				if (otherEntry.getMacroValue() != null) {
					return false;
				}
			} else {
				if (!macroValue.equals(otherEntry.getMacroValue())) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}

}
