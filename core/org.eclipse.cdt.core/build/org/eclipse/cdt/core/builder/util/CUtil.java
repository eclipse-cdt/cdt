/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.util;

/**
 * @author sam.robb
 * 
 * Collection of generic utility functions.
 */
public class CUtil {

	/**
	 * Given a name, this function will decide whether the
	 * name conforms to rules for naming valid C identifiers.
	 */
	public static boolean isValidCIdentifier(String name) {

		// any sequence of letters, digits, or underscores,
		// which begins with a letter or underscore

		if ((name == null) || (name.length() < 1)) {
			return false;
		}

		char c = name.charAt(0);

		if ((c != '_') && !Character.isLetter(c)) {
			return false;
		}

		for (int i = 1; i < name.length(); i++) {
			c = name.charAt(i);
			if ((c != '_') && !Character.isLetterOrDigit(c)) {
				return false;
			}
		}

		return true;
	}

}
