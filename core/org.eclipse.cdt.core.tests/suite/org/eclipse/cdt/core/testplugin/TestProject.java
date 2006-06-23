/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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
