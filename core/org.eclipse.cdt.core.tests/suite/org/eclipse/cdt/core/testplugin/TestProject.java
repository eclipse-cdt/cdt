/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public void configure(ICDescriptor cDescriptor) throws CoreException {
		cDescriptor.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, TestScannerProvider.SCANNER_ID);
	}

	@Override
	public void update(ICDescriptor cDescriptor, String extensionID) throws CoreException {
		if (extensionID.equals(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID)) {
			cDescriptor.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, TestScannerProvider.SCANNER_ID);
		}
	}
}
