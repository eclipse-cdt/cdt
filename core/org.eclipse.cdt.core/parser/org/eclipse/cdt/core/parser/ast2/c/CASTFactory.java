/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.ast2.c;

/**
 * @author Doug Schaefer
 */
public class CASTFactory {

	/**
	 * Factory method to parse a string of code with no scanner info
	 * and return an ICASTTranslationUnit object representing the
	 * parse tree.
	 * 
	 * @param code
	 * @return
	 */
	public static ICASTTranslationUnit parseString(StringBuffer code) {
		return null;
	}
}
