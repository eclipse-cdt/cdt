/*******************************************************************************
 *  Copyright (c) 2008 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
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
