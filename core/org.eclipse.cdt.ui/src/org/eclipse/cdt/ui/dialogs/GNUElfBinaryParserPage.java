/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui.dialogs;

/**
 * Reusing AbstractGNUBinaryParserPage.
 * New class is required for the algorithm in method performApply.
 * Must implement getRealBinaryParserPage method. 
 * 
 * @author vhirsl
 */
public class GNUElfBinaryParserPage extends AbstractGNUBinaryParserPage {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.AbstractGNUBinaryParserPage#getRealBinaryParserPage()
	 */
	protected AbstractGNUBinaryParserPage getRealBinaryParserPage() {
		return this;
	}

}
