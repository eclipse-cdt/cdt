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

public class MacroEntry extends ACPathEntry implements IMacroEntry {

	IPath resourcePath;
	String macroName;
	String macroValue;

	public MacroEntry (IPath resourcePath, String macroName, String macroValue,
		boolean isRecursive, IPath[] exclusionPatterns, boolean isExported) {
		super(IMacroEntry.CDT_MACRO, isRecursive, exclusionPatterns, isExported);
		this.resourcePath = resourcePath;
		this.macroName = macroName;
		this.macroValue = macroValue;
	}

	/**
	 * Returns the absolute path from the worskspace root or
	 * relative path of the affected resource.
	 * @return String
	 */
	public IPath getResourcePath() {
		return resourcePath;
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
		return macroName;
	}

}
