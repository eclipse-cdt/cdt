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
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerData;

/**
 * @author jcamelon
 */
public interface IScannerExtension  {

	public void setupBuiltInMacros(IScannerData scannerData);
	
	public boolean isExtensionKeyword(ParserLanguage language, char[] tokenImage);
	public boolean isExtensionOperator(ParserLanguage language, char[] query);
	
	public IToken  createExtensionToken(IScannerData scannerData, char[] image);

}
