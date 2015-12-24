/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IResource;

public class TestScannerProvider extends AbstractCExtension implements IScannerInfoProvider {
	public static String[] sIncludes;
	public static String[] sLocalIncludes;
	public static String[] sIncludeFiles;
	public static String[] sMacroFiles;
	public static Map<String, String> sDefinedSymbols = new HashMap<>();
	public final static String SCANNER_ID = CTestPlugin.PLUGIN_ID + ".TestScanner";

	public static void clear() {
		sIncludes= sLocalIncludes= sIncludeFiles= sMacroFiles= null;
		sDefinedSymbols.clear();
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		return new TestScannerInfo(sIncludes, sLocalIncludes, sMacroFiles, sIncludeFiles, sDefinedSymbols);
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
	}
}
