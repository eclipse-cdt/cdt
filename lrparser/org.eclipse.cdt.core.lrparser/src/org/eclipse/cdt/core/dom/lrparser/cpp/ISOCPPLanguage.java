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
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
import org.eclipse.cdt.core.dom.lrparser.action.c99.C99ASTNodeFactory;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPParser;

/**
 * ILanguage implementation for the C++ parser.
 * 
 * @author Mike Kucera
 */
public class ISOCPPLanguage extends BaseExtensibleLanguage {

	public static final String PLUGIN_ID = "org.eclipse.cdt.core.lrparser"; //$NON-NLS-1$ 
	public static final String ID = PLUGIN_ID + ".isocpp"; //$NON-NLS-1$ 
	
	private static final ITokenMap TOKEN_MAP = DOMToISOCPPTokenMap.DEFAULT_MAP;
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
	protected ITokenMap getTokenMap() {
		return TOKEN_MAP;
	}

	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
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
	protected IASTTranslationUnit createASTTranslationUnit() {
		return C99ASTNodeFactory.DEFAULT_INSTANCE.newTranslationUnit();
	}

	@Override
	protected ParserLanguage getParserLanguageForPreprocessor() {
		return ParserLanguage.CPP;
	}

	

}
