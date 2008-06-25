/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.parser.cpp;

import org.eclipse.cdt.core.parser.IToken;

/**
 * Configures the preprocessor for parsing POP C++, 
 * see <a href=http://gridgroup.tic.hefr.ch/popc/index.php/Documentation>Documentation</a>
 * @since 5.1
 */
public class POPCPPScannerExtensionConfiguration extends GPPScannerExtensionConfiguration {
	private static POPCPPScannerExtensionConfiguration sInstance= new POPCPPScannerExtensionConfiguration();
	public static POPCPPScannerExtensionConfiguration getInstance() {
		return sInstance;
	}
	@SuppressWarnings("nls")
	protected POPCPPScannerExtensionConfiguration() {
		addMacro("@pack(...)", "");
		addMacro("__concat__(x,y)",  "x##y"); 
		addMacro("__xconcat__(x,y)",  "__concat__(x,y)"); 
		addMacro("@",  "; void __xconcat__(@, __LINE__)()");

		addKeyword("parclass".toCharArray(), IToken.t_class);
	}

	@Override
	public boolean supportAtSignInIdentifiers() {
		return true;
	}
}
