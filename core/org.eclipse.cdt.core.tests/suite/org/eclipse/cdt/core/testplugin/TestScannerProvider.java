/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
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

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IResource;

public class TestScannerProvider extends AbstractCExtension implements IScannerInfoProvider {

	public static String[] sIncludes= null;
	public static String[] sIncludeFiles= null;
	public static String[] sMacroFiles= null;
	public final static String SCANNER_ID = CTestPlugin.PLUGIN_ID + ".TestScanner";
	
	public static void clear() {
		sIncludes= sIncludeFiles= sMacroFiles= null;
	}
	
	public IScannerInfo getScannerInformation(IResource resource) {
		return new TestScannerInfo(sIncludes, sIncludeFiles, sMacroFiles);
	}

	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
	}
}
