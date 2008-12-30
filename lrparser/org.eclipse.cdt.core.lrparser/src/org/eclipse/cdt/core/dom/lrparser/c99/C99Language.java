/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.c99;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.IDOMTokenMap;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.ScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.CLanguageKeywords;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parser;
import org.eclipse.cdt.internal.core.dom.parser.c.CNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;

/**
 * ILanguage implementation for the C99 parser.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public class C99Language extends BaseExtensibleLanguage {

	public static final String PLUGIN_ID = "org.eclipse.cdt.core.lrparser"; //$NON-NLS-1$ 
	public static final String ID = PLUGIN_ID + ".c99"; //$NON-NLS-1$ 
	
	private static final IDOMTokenMap TOKEN_MAP = DOMToC99TokenMap.DEFAULT_MAP;
	private static final IScannerExtensionConfiguration SCANNER_CONFIGURATION = ScannerExtensionConfiguration.createC();
	
	private static C99Language DEFAULT = new C99Language();
	
	
	public static C99Language getDefault() {
		return DEFAULT;
	}
	
	@Override
	protected IParser getParser() {
		return new C99Parser();
	}

	@Override
	protected IDOMTokenMap getTokenMap() {
		return TOKEN_MAP;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return SCANNER_CONFIGURATION;
	}
	
	public IContributedModelBuilder createModelBuilder(@SuppressWarnings("unused") ITranslationUnit tu) {
		return null;
	}

	public String getId() {
		return ID;
	}

	public int getLinkageID() {
		return ILinkage.C_LINKAGE_ID;
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.C;
	}

	private ICLanguageKeywords cLanguageKeywords = new CLanguageKeywords(ParserLanguage.C, SCANNER_CONFIGURATION);
	

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if(IPDOMLinkageFactory.class.equals(adapter))
			return new PDOMCLinkageFactory();
		if(ICLanguageKeywords.class.equals(adapter))
			return cLanguageKeywords;
		
		return super.getAdapter(adapter);
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

	
	

}
