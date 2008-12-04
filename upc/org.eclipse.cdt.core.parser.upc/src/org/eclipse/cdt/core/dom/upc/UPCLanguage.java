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

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.IDOMTokenMap;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.ScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.upc.DOMToUPCTokenMap;
import org.eclipse.cdt.core.dom.parser.upc.UPCKeyword;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.c.CNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.upc.UPCParser;


/**
 * Implementation of the ILanguage extension point, adds UPC as a language to CDT.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public class UPCLanguage extends BaseExtensibleLanguage {
	
	public static final String PLUGIN_ID = "org.eclipse.cdt.core.parser.upc"; //$NON-NLS-1$ 
	public static final String ID = PLUGIN_ID + ".upc"; //$NON-NLS-1$ 
	
	private static final IDOMTokenMap TOKEN_MAP = new DOMToUPCTokenMap();
	
	private static final UPCLanguage myDefault  = new UPCLanguage();
	private static final String[] upcKeywords = UPCKeyword.getAllKeywords();

	private static final IScannerExtensionConfiguration SCANNER_CONFIGURATION = new ScannerExtensionConfiguration();
	
	
	public static UPCLanguage getDefault() {
		return myDefault;
	}
	
	@Override
	protected IDOMTokenMap getTokenMap() {
		return TOKEN_MAP;
	}
	
	@Override
	public IParser getParser() {
		return new UPCParser();
	}

	/**
	 * @param tu Not used, default model builder used. 
	 */
	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		return null;
	}
	
	public String getId() {
		return ID;
	}
	
	public int getLinkageID() {
		return ILinkage.C_LINKAGE_ID;
	}

	@Override
	public String[] getKeywords() {
		return upcKeywords;
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.C;
	}

	/**
	 * Gets the translation unit object and sets the index and the location resolver. 
	 */
	@Override
	protected IASTTranslationUnit createASTTranslationUnit(IIndex index, IScanner preprocessor) {
		IASTTranslationUnit tu = CNodeFactory.getDefault().newTranslationUnit();
		tu.setIndex(index);
		if(tu instanceof CASTTranslationUnit) {
			((CASTTranslationUnit)tu).setLocationResolver(preprocessor.getLocationResolver());
		}
		return tu;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return SCANNER_CONFIGURATION;
	}

}
