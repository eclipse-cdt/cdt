/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IPath;

/**
 */
public class PathEntryContainerChanged  {

	/**
	 * Change in the includes settings
	 */
	public static final int INCLUDE_CHANGED = 1;

	/**
	 * Change in the Macro
	 */
	public static final int MACRO_CHANGED = 2;

	/**
	 * Type of changes
	 */
	int fType;

	/**
	 * Affected file
	 */
	IPath fPath;

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3257565105200705590L;


	/**
	 * 
	 * @param source
	 * @param type
	 */
	public PathEntryContainerChanged(IPath source, int type) {
		fPath = source;
		fType = type;
	}

	/**
	 * Returns the affected path;
	 * @return path
	 */
	public IPath getPath() {
		return fPath;
	}

	/**
	 * Type of change.
	 * @return
	 */
	public int getType() {
		return fType;
	}

	/**
	 * whether or not the change affected the include paths
	 * @return
	 */
	public boolean isIncludeChange() {
		return (fType & INCLUDE_CHANGED) != 0;
	}

	/**
	 * Whether or not the chage affected the macro entries
	 * @return
	 */
	public boolean isMacroChange() {
		return (fType & MACRO_CHANGED) != 0;
	}

}
