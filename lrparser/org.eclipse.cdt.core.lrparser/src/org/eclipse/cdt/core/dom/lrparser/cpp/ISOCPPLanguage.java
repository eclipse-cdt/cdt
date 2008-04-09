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
package org.eclipse.cdt.core.dom.lrparser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.IDOMTokenMap;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.action.cpp.CPPASTNodeFactory;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;

/**
 * ILanguage implementation for the C++ parser.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public class ISOCPPLanguage extends BaseExtensibleLanguage {

	public static final String PLUGIN_ID = "org.eclipse.cdt.core.lrparser"; //$NON-NLS-1$ 
	public static final String ID = PLUGIN_ID + ".isocpp"; //$NON-NLS-1$ 
	
	private static final IDOMTokenMap TOKEN_MAP = DOMToISOCPPTokenMap.DEFAULT_MAP;
	private static GPPLanguage GPP_LANGUAGE = GPPLanguage.getDefault();
	
	
	private static ISOCPPLanguage DEFAULT = new ISOCPPLanguage();
	
	
	public static ISOCPPLanguage getDefault() {
		return DEFAULT;
	}
	
	@Override
	protected IParser getParser() {
		return new CPPParser();
	}

	@Override
	protected IDOMTokenMap getTokenMap() {
		return TOKEN_MAP;
	}

	public IContributedModelBuilder createModelBuilder(@SuppressWarnings("unused") ITranslationUnit tu) {
		return null;
	}

	public String getId() {
		return ID;
	}

	public int getLinkageID() {
		return ILinkage.CPP_LINKAGE_ID;
	}

	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length) {
		return GPP_LANGUAGE.getSelectedNames(ast, start, length);
	}

	public String[] getBuiltinTypes() {
		return GPP_LANGUAGE.getBuiltinTypes();
	}

	public String[] getKeywords() {
		return GPP_LANGUAGE.getKeywords();
	}

	public String[] getPreprocessorKeywords() {
		return GPP_LANGUAGE.getPreprocessorKeywords();
	}

	@Override
	protected ParserLanguage getParserLanguageForPreprocessor() {
		return ParserLanguage.CPP;
	}

	/**
	 * Gets the translation unit object and sets the index and the location resolver. 
	 */
	@Override
	protected IASTTranslationUnit createASTTranslationUnit(IIndex index, IScanner preprocessor) {
		IASTTranslationUnit tu = CPPASTNodeFactory.DEFAULT_INSTANCE.newTranslationUnit();
		tu.setIndex(index);
		if(tu instanceof CPPASTTranslationUnit) {
			((CPPASTTranslationUnit)tu).setLocationResolver(preprocessor.getLocationResolver());
		}
		return tu;
	}
	

}
