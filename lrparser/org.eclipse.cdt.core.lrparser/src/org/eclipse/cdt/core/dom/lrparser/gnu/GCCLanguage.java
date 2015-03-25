/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.gnu;

import java.util.Map;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.lrparser.gcc.GCCParser;

/**
 * ILanguage implementation for the C99 parser.
 * 
 * @author Mike Kucera
 */
public class GCCLanguage extends BaseExtensibleLanguage {
	public static final String ID = "org.eclipse.cdt.core.lrparser.gcc"; //$NON-NLS-1$ 
	
	private static GCCLanguage DEFAULT = new GCCLanguage();
	
	public static GCCLanguage getDefault() {
		return DEFAULT;
	}
	
	@Override
	protected IParser<IASTTranslationUnit> getParser(IScanner scanner, IIndex index, Map<String,String> properties) {
		return new GCCParser(scanner, DOMToGCCTokenMap.DEFAULT_MAP, getBuiltinBindingsProvider(), index, properties);
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return GCCScannerExtensionConfiguration.getInstance();
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public int getLinkageID() {
		return ILinkage.C_LINKAGE_ID;
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.C;
	}

	protected IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new GCCParserExtensionConfiguration().getBuiltinBindingsProvider();
	}
}
