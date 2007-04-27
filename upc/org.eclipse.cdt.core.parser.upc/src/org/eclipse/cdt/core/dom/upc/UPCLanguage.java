/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.upc;

import org.eclipse.cdt.core.dom.c99.C99Language;
import org.eclipse.cdt.core.dom.c99.IParser;
import org.eclipse.cdt.core.dom.parser.c99.C99KeywordMap;
import org.eclipse.cdt.core.dom.parser.upc.UPCKeywordMap;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCParser;


/**
 * Implementation of the ILanguage extension point, adds UPC as a language to CDT.
 *
 * Just hooks into C99Language and provides UPC specifier parser and keyword map.
 *
 * @author Mike Kucera
 */
public class UPCLanguage extends C99Language {
	
	// TODO: this should probably go somewhere else
	public static final String PLUGIN_ID = "org.eclipse.cdt.core.parser.upc"; //$NON-NLS-1$ 
	public static final String ID = PLUGIN_ID + ".upc"; //$NON-NLS-1$ 
	
	private static UPCKeywordMap keywordMap = new UPCKeywordMap();
	
	
	
	public String getId() {
		return ID;
	}

	public String getName() {
		// TODO: this has to be read from a message bundle
		return "UPC";//$NON-NLS-1$
	}
	
	
	protected IParser getParser() {
		return new UPCParser();
	}

	public C99KeywordMap getKeywordMap() {
		return keywordMap;
	}

}
