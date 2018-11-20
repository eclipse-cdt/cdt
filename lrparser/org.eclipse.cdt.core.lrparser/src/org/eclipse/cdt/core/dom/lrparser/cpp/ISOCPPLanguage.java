/*******************************************************************************
 *  Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.cpp;

import java.util.Map;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.ScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPParser;

/**
 * ILanguage implementation for the C++ parser.
 *
 * @author Mike Kucera
 */
public class ISOCPPLanguage extends BaseExtensibleLanguage {

	public static final String ID = "org.eclipse.cdt.core.lrparser.isocpp"; //$NON-NLS-1$

	private static ISOCPPLanguage DEFAULT = new ISOCPPLanguage();

	public static ISOCPPLanguage getDefault() {
		return DEFAULT;
	}

	@Override
	protected IParser<IASTTranslationUnit> getParser(IScanner scanner, IIndex index, Map<String, String> properties) {
		return new CPPParser(scanner, DOMToISOCPPTokenMap.DEFAULT_MAP, getBuiltinBindingsProvider(), index, properties);
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return ScannerExtensionConfiguration.createCPP();
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public int getLinkageID() {
		return ILinkage.CPP_LINKAGE_ID;
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.CPP;
	}

	protected IBuiltinBindingsProvider getBuiltinBindingsProvider() {
		return new ANSICPPParserExtensionConfiguration().getBuiltinBindingsProvider();
	}
}
