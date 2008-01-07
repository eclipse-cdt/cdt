/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *    Anton Leherbauer (Wind River Systems)
 *    Mike Kucera - IBM
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.parser.AbstractCLikeLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPLinkageFactory;

/**
 * Concrete ILanguage implementation for the DOM C++ parser.
 *
 */
public class GPPLanguage extends AbstractCLikeLanguage {

	protected static final GPPScannerExtensionConfiguration CPP_GNU_SCANNER_EXTENSION = new GPPScannerExtensionConfiguration();
	protected static final GPPParserExtensionConfiguration CPP_GNU_PARSER_EXTENSION = new GPPParserExtensionConfiguration();
	public static final String ID = CCorePlugin.PLUGIN_ID + ".g++"; //$NON-NLS-1$

	private static final GPPLanguage DEFAULT_INSTANCE = new GPPLanguage();
	
	public static GPPLanguage getDefault() {
		return DEFAULT_INSTANCE;
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IPDOMLinkageFactory.class) {
			return new PDOMCPPLinkageFactory();
		}
		return super.getAdapter(adapter);
	}
	
	public String getId() {
		return ID;
	}
	
	
	public int getLinkageID() {
		return ILinkage.CPP_LINKAGE_ID;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return CPP_GNU_SCANNER_EXTENSION;
	}

	@Override
	protected ISourceCodeParser createParser(IScanner scanner, ParserMode parserMode, IParserLogService logService, IIndex index) {
		return new GNUCPPSourceParser(scanner, parserMode, logService, CPP_GNU_PARSER_EXTENSION, index);
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.CPP;
	}

}
