/*******************************************************************************
 *  Copyright (c) 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.upc;

import org.eclipse.cdt.core.dom.parser.CLanguageKeywords;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.ParserLanguage;

public class UPCLanguageKeywords extends CLanguageKeywords {

	public UPCLanguageKeywords(IScannerExtensionConfiguration config) {
		super(ParserLanguage.C, config);
	}

	private static String[] upcKeywords = UPCKeyword.getAllKeywords();
	
	@Override
	public String[] getKeywords() {
		return upcKeywords;
	}
}
