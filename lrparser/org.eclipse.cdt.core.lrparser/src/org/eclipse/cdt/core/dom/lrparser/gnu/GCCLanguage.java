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
package org.eclipse.cdt.core.dom.lrparser.gnu;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.IDOMTokenMap;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.lrparser.gcc.GCCParser;
import org.eclipse.cdt.internal.core.dom.parser.c.CNodeFactory;

/**
 * ILanguage implementation for the C99 parser.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public class GCCLanguage extends BaseExtensibleLanguage {

	public static final String ID = "org.eclipse.cdt.core.lrparser.gcc"; //$NON-NLS-1$ 
	
	private static GCCLanguage DEFAULT = new GCCLanguage();
	
	public static GCCLanguage getDefault() {
		return DEFAULT;
	}
	
	@Override
	protected IParser getParser() {
		return new GCCParser();
	}

	@Override
	protected IDOMTokenMap getTokenMap() {
		return DOMToGCCTokenMap.DEFAULT_MAP;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return GCCScannerExtensionConfiguration.getInstance();
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
