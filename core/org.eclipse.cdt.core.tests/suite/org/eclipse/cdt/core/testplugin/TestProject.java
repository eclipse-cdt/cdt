/*
 * Created on Jan 14, 2004
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.testplugin;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICOwner;
import org.eclipse.core.runtime.CoreException;

public class TestProject implements ICOwner {

	public void configure(ICDescriptor cDescriptor) throws CoreException {
		cDescriptor.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, TestScannerProvider.SCANNER_ID);
	}

	public void update(ICDescriptor cDescriptor, String extensionID) throws CoreException {
		if ( extensionID.equals(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID)) {
			cDescriptor.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, TestScannerProvider.SCANNER_ID);
		}
	}
}
