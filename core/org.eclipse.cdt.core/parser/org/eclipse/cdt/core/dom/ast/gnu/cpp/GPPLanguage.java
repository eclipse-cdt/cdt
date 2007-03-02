/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.gnu.Messages;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPLanguage;
import org.eclipse.cdt.core.dom.parser.cpp.AbstractCPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;

/**
 * @author Doug Schaefer
 *
 */
public class GPPLanguage extends AbstractCPPLanguage {

	protected static final GPPScannerExtensionConfiguration CPP_GNU_SCANNER_EXTENSION = new GPPScannerExtensionConfiguration();
	protected static final GPPParserExtensionConfiguration CPP_GNU_PARSER_EXTENSION = new GPPParserExtensionConfiguration();
	public static final String ID = CCorePlugin.PLUGIN_ID + ".g++"; //$NON-NLS-1$

	private static final GPPLanguage myDefault = new GPPLanguage();
	
	public static GPPLanguage getDefault() {
		return myDefault;
	}
	
	public String getId() {
		return ID;
	}
	
	public String getName() {
		return Messages.getString("GPPLanguage.name"); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.AbstractCPPLanguage#getScannerExtensionConfiguration()
	 */
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return CPP_GNU_SCANNER_EXTENSION;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.AbstractCPPLanguage#getParserExtensionConfiguration()
	 */
	protected AbstractCPPParserExtensionConfiguration getParserExtensionConfiguration() {
		return CPP_GNU_PARSER_EXTENSION;
	}

}
