/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IPath;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
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
	 */
	public int getType() {
		return fType;
	}

	/**
	 * whether or not the change affected the include paths
	 */
	public boolean isIncludeChange() {
		return (fType & INCLUDE_CHANGED) != 0;
	}

	/**
	 * Whether or not the change affected the macro entries
	 */
	public boolean isMacroChange() {
		return (fType & MACRO_CHANGED) != 0;
	}

}
