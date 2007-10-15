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

import org.eclipse.cdt.core.dom.c99.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.c99.IC99TokenCollector;
import org.eclipse.cdt.core.dom.c99.IKeywordMap;
import org.eclipse.cdt.core.dom.c99.ILexerFactory;
import org.eclipse.cdt.core.dom.c99.IPPTokenComparator;
import org.eclipse.cdt.core.dom.c99.IParser;
import org.eclipse.cdt.core.dom.c99.IPreprocessorExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c99.GCCPreprocessorExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.upc.UPCKeywordMap;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCLexerFactory;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCPPTokenComparator;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCParser;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCTokenCollector;


/**
 * Implementation of the ILanguage extension point, adds UPC as a language to CDT.
 *
 * Just hooks into C99Language and provides UPC specifier parser and keyword map.
 *
 * @author Mike Kucera
 */
public class UPCLanguage extends BaseExtensibleLanguage {
	
	protected static final IPreprocessorExtensionConfiguration 
		GCC_PREPROCESSOR_EXTENSION = new GCCPreprocessorExtensionConfiguration();
	
	// TODO: this should probably go somewhere else
	public static final String PLUGIN_ID = "org.eclipse.cdt.core.parser.upc"; //$NON-NLS-1$ 
	public static final String ID = PLUGIN_ID + ".upc"; //$NON-NLS-1$ 
	
	private static final UPCKeywordMap keywordMap = new UPCKeywordMap();
	private static final UPCLanguage   myDefault  = new UPCLanguage();
	
	
	public static UPCLanguage getDefault() {
		return myDefault;
	}
	
	public String getId() {
		return ID;
	}

	public String getName() {
		// TODO: this has to be read from a message bundle
		return "UPC";//$NON-NLS-1$
	}
	
	
	public IParser getParser() {
		return new UPCParser();
	}

	public IKeywordMap getKeywordMap() {
		return keywordMap;
	}

	protected IPreprocessorExtensionConfiguration getPreprocessorExtensionConfiguration() {
		return GCC_PREPROCESSOR_EXTENSION;
	}

	protected ILexerFactory getLexerFactory() {
		return new UPCLexerFactory();
	}

	protected IPPTokenComparator getTokenComparator() {
		return new UPCPPTokenComparator();
	}
	
	protected IC99TokenCollector getTokenCollector() {
		return new UPCTokenCollector();
	}
}
