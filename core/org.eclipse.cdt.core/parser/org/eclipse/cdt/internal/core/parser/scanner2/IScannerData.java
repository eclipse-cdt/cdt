/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * @author jcamelon
 */
public interface IScannerData {

	/**
	 * @return Returns the language.
	 */
	public abstract ParserLanguage getLanguage();

	/**
	 * @return
	 */
	public int getCurrentOffset();


	/**
	 * @param o
	 * @return
	 */
	public int getLineNumber(int o);


	/**
	 * @return
	 */
	public char [] getCurrentFilename();


	/**
	 * @return
	 */
	public CharArrayObjectMap getRealDefinitions();
}