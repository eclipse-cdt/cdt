/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.upc;

import java.util.Map;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.ScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.upc.DOMToUPCTokenMap;
import org.eclipse.cdt.core.dom.parser.upc.UPCLanguageKeywords;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCParser;


/**
 * Implementation of the ILanguage extension point, adds UPC as a language to CDT.
 *
 * @author Mike Kucera
 */
public class UPCLanguage extends BaseExtensibleLanguage {

	public static final String ID = "org.eclipse.cdt.core.parser.upc.upc"; //$NON-NLS-1$

	public static final String UPC_CONTENT_TYPE_ID = "org.eclipse.cdt.core.parser.upc.upcSource"; //$NON-NLS-1$

	private static final UPCLanguage myDefault  = new UPCLanguage();

	public static UPCLanguage getDefault() {
		return myDefault;
	}

	@Override
	protected IParser<IASTTranslationUnit> getParser(IScanner scanner, IIndex index, Map<String,String> properties) {
		return new UPCParser(scanner, new DOMToUPCTokenMap(), getBuiltinBindingsProvider(), index, properties);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public int getLinkageID() {
		return ILinkage.C_LINKAGE_ID;
	}


	private static final ICLanguageKeywords upcKeywords = new UPCLanguageKeywords(ScannerExtensionConfiguration.createC());

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if(ICLanguageKeywords.class.equals(adapter))
			return upcKeywords;

		return super.getAdapter(adapter);
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.C;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return ScannerExtensionConfiguration.createC();
	}

	protected IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new ANSICParserExtensionConfiguration().getBuiltinBindingsProvider();
	}
}
