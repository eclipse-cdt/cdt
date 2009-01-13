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
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parser;
import org.eclipse.cdt.internal.core.dom.parser.c.CNodeFactory;

/**
 * ILanguage implementation for the C99 parser.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public class C99Language extends BaseExtensibleLanguage {

	public static final String ID = "org.eclipse.cdt.core.lrparser.c99"; //$NON-NLS-1$ 
	
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
		return DOMToC99TokenMap.DEFAULT_MAP;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return ScannerExtensionConfiguration.createC();
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

	@Override
	protected IASTTranslationUnit createASTTranslationUnit() {
		return CNodeFactory.getDefault().newTranslationUnit();
	}

}
