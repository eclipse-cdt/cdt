/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

/**
 * Reusing AbstractGNUBinaryParserPage.
 * New class is required for the algorithm in method performApply.
 * Must implement getRealBinaryParserPage method.
 *
 * @author vhirsl
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GNUXCoffBinaryParserPage extends AbstractGNUBinaryParserPage {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.GNUElfBinaryParserPage#getRealBinaryParserPage()
	 */
	@Override
	protected AbstractGNUBinaryParserPage getRealBinaryParserPage() {
		return this;
	}
}
