/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.internal.filetype;

/**
 * Utility class used to check arguments and generate an
 * IllegalArgumentException if they fail to meet some
 * minimum requirements (null ref, empty string, etc.)
 */
public class Argument {

	/**
	 * Throw an exception if a string argument is null or empty.
	 * 
	 * @param arg String to check
	 * 
	 * @throws IllegalArgumentException
	 */
	static public void check(String arg) throws IllegalArgumentException {
		check(arg, false);
	}

	/**
	 * Throw an exception if a string argument is null (and optionally,
	 * if the string is empty).
	 * 
	 * @param arg String to check
	 * @param allowEmpty True to allow an empty string.
	 * 
	 * @throws IllegalArgumentException
	 */
	static public void check(String arg, boolean allowEmpty) throws IllegalArgumentException {
		if (null == arg) {
			throw new IllegalArgumentException("Null string argument"); //$NON-NLS-1$
		} else if (0 == arg.length()) {
			throw new IllegalArgumentException("Empty string argument"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Throws an exception if an object argument is null.
	 * 
	 * @param arg Object to check
	 * 
	 * @throws IllegalArgumentException
	 */
	static public void check(Object arg) throws IllegalArgumentException {
		if (null == arg) {
			throw new IllegalArgumentException("Null reference argument"); //$NON-NLS-1$
		}
	}

	/**
	 * Throws an exception if an integer argument lies outside the specified
	 * range.
	 * 
	 * @param arg Integer to check
	 * @param min Minimum allowed value for the argument.
	 * @param max Maximum allowed value for the argument.
	 * 
	 * @throws IllegalArgumentException
	 */
	static public void check(int arg, int min, int max) throws IllegalArgumentException {
		if (arg < min) {
			throw new IllegalArgumentException("Integer argument out of range (low)"); //$NON-NLS-1$
		} else if (arg > max) {
			throw new IllegalArgumentException("Integer argument out of range (high)"); //$NON-NLS-1$
		}
	}

	
}
