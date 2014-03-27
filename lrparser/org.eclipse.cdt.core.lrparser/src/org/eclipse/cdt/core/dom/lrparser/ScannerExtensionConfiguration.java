/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import org.eclipse.cdt.core.dom.parser.AbstractScannerExtensionConfiguration;


/**
 * A minimalistic scanner configuration for the LR parser.
 * 
 * @author Mike Kucera
 *
 */
@SuppressWarnings("nls")
public class ScannerExtensionConfiguration extends AbstractScannerExtensionConfiguration {

	
	private ScannerExtensionConfiguration() {
	}
	
	public static ScannerExtensionConfiguration createC() {
		ScannerExtensionConfiguration sec = new ScannerExtensionConfiguration();
		sec.addMacro("__null", "(void *)0"); 
		return sec;
	}
	
	public static ScannerExtensionConfiguration createCPP() {
		ScannerExtensionConfiguration sec = new ScannerExtensionConfiguration();
		sec.addMacro("__null", "0");
		return sec;
	}

	@Override
	public boolean support$InIdentifiers() {
		return true;
	}

	@Override
	public boolean supportUserDefinedLiterals() {
		return false;
	}
}
