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

package org.eclipse.cdt.core.dom.ast.gnu.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.gnu.Messages;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.AbstractCLanguage;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;

/**
 * @author Doug Schaefer
 *
 */
public class GCCLanguage extends AbstractCLanguage {

	protected static final GCCScannerExtensionConfiguration C_GNU_SCANNER_EXTENSION = new GCCScannerExtensionConfiguration();
	protected static final GCCParserExtensionConfiguration C_GNU_PARSER_EXTENSION = new GCCParserExtensionConfiguration();
	// Must match the id in the extension
	public static final String ID = CCorePlugin.PLUGIN_ID + ".gcc"; //$NON-NLS-1$ 

	private static final GCCLanguage myDefault = new GCCLanguage();
	
	public static GCCLanguage getDefault() {
		return myDefault;
	}
	
	public String getId() {
		return ID; 
	}
	
	public String getName() {
		return Messages.getString("GCCLanguage.name"); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.cdt.core.parser.AbstractCLanguage#getParserExtensionConfiguration()
	 */
	protected ICParserExtensionConfiguration getParserExtensionConfiguration() {
		return C_GNU_PARSER_EXTENSION;
	}

	/*
	 * @see org.eclipse.cdt.core.parser.AbstractCLanguage#getScannerExtensionConfiguration()
	 */
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return C_GNU_SCANNER_EXTENSION;
	}
	
}
