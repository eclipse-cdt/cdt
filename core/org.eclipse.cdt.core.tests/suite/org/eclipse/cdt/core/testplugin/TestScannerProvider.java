/*
 * Created on Jan 16, 2004
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.testplugin;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IResource;

public class TestScannerProvider extends AbstractCExtension implements IScannerInfoProvider {

	public final static String SCANNER_ID = CTestPlugin.PLUGIN_ID + ".TestScanner";
	
	public IScannerInfo getScannerInformation(IResource resource) {
		return new TestScannerInfo();
	}

	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
	}
}
