/*******************************************************************************
 * Copyright (c) 2023 Julian Waters.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.parser.AbstractCLikeLanguage;
import org.eclipse.cdt.core.dom.parser.ICFamilyLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.CPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSourceParser;

/**
 * ILanguage implementation for the DOM no-dialect, pure C++ Parser.
 */
public class CPPLanguage extends AbstractCLikeLanguage implements ICFamilyLanguage {

	public static final ANSICPPParserExtensionConfiguration CPP_PARSER_EXTENSION = new ANSICPPParserExtensionConfiguration();

	@Override
	public String getId() {
		// Must match the id in the extension
		return CCorePlugin.PLUGIN_ID + ".c++"; //$NON-NLS-1$
	}

	@Override
	public int getLinkageID() {
		return ILinkage.CPP_LINKAGE_ID;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration(IScannerInfo info) {
		return CPPScannerExtensionConfiguration.getConfiguration(info);
	}

	@Override
	protected ISourceCodeParser createParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			IIndex index) {
		return new CPPSourceParser(scanner, parserMode, logService, CPP_PARSER_EXTENSION, index);
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.CPP;
	}

	@Override
	public boolean isPartOfCFamily() {
		return true;
	}

}
