/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.cdt.ui.text.AbstractCScanner;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;

/**
 *
 */
public final class SingleTokenCScanner extends AbstractCScanner {

	public SingleTokenCScanner(ITokenStoreFactory factory, String property) {
		super(factory.createTokenStore(new String[] { property }), 20);
		setDefaultReturnToken(getToken(property));
	}

}
