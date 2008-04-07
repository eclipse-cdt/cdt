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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.IDOMTokenMap;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.action.c99.C99ASTNodeFactory;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.parser.upc.DOMToUPCTokenMap;
import org.eclipse.cdt.core.dom.parser.upc.UPCKeyword;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;


/**
 * Implementation of the ILanguage extension point, adds UPC as a language to CDT.
 * 
 * @author Mike Kucera
 */
public class UPCLanguage extends BaseExtensibleLanguage {
	
	public static final String PLUGIN_ID = "org.eclipse.cdt.core.parser.upc"; //$NON-NLS-1$ 
	public static final String ID = PLUGIN_ID + ".upc"; //$NON-NLS-1$ 
	
	private static final IDOMTokenMap TOKEN_MAP = new DOMToUPCTokenMap();
	private static final C99Language C99_LANGUAGE = C99Language.getDefault();
	
	private static final UPCLanguage myDefault  = new UPCLanguage();
	private static final String[] keywords = UPCKeyword.getAllKeywords();

	
	public static UPCLanguage getDefault() {
		return myDefault;
	}
	
	@Override
	protected IDOMTokenMap getTokenMap() {
		return TOKEN_MAP;
	}
	
	@Override
	public IParser getParser() {
		//return new UPCParser();
		return null;
	}

	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		return null;
	}
	
	public String getId() {
		return ID;
	}
	
	public int getLinkageID() {
		return ILinkage.C_LINKAGE_ID;
	}
	
	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length) {
		return C99_LANGUAGE.getSelectedNames(ast, start, length);
	}

	public String[] getBuiltinTypes() {
		return C99_LANGUAGE.getBuiltinTypes();
	}

	public String[] getKeywords() {
		return keywords;
	}

	public String[] getPreprocessorKeywords() {
		return C99_LANGUAGE.getPreprocessorKeywords();
	}

	@Override
	protected ParserLanguage getParserLanguageForPreprocessor() {
		return ParserLanguage.C;
	}

	/**
	 * Gets the translation unit object and sets the index and the location resolver. 
	 */
	@SuppressWarnings("restriction")
	@Override
	protected IASTTranslationUnit createASTTranslationUnit(IIndex index, IScanner preprocessor) {
		IASTTranslationUnit tu = C99ASTNodeFactory.DEFAULT_INSTANCE.newTranslationUnit();
		tu.setIndex(index);
		if(tu instanceof CASTTranslationUnit) {
			((CASTTranslationUnit)tu).setLocationResolver(preprocessor.getLocationResolver());
		}
		return tu;
	}
	
	
	



}
