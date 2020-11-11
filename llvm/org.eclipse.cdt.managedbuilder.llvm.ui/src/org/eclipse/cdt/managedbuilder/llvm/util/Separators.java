/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Petri Tuononen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.util;

/**
 * Provides different separators such as file and path separators.
 *
 */
public class Separators {

	private static final String pathSeparator = System.getProperty("path.separator"); //$NON-NLS-1$
	private static final String fileSeparator = System.getProperty("file.separator"); //$NON-NLS-1$

	/**
	 * Get path separator.
	 *
	 * @return  path separator
	 */
	public static String getPathSeparator() {
		return pathSeparator;
	}

	/**
	 * Get file separator.
	 *
	 * @return  file separator
	 */
	public static String getFileSeparator() {
		return fileSeparator;
	}

}
