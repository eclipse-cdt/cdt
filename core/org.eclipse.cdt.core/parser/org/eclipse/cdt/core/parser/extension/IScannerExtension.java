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
package org.eclipse.cdt.core.parser.extension;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.scanner.IScannerData;

/**
 * @author jcamelon
 */
public interface IScannerExtension  {

	public String initializeMacroValue( IScannerData scannerData, String original );
	public void setupBuiltInMacros(IScannerData scannerData, ParserLanguage language);

	public boolean canHandlePreprocessorDirective( String directive );
	public void handlePreprocessorDirective( IScannerData scannerData, String directive, String restOfLine );
	
	public boolean isExtensionKeyword(String tokenImage);
	public IToken  createExtensionToken(String image, IScannerData scannerData);

	/**
	 * @return
	 */
	public boolean offersDifferentIdentifierCharacters();

	/**
	 * @param c
	 * @return
	 */
	public boolean isValidIdentifierStartCharacter(int c);
	public boolean isValidIdentifierCharacter( int c );
}
