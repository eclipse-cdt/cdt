/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
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
		super(factory.createTokenStore(new String[] {property}), 20);
		setDefaultReturnToken(getToken(property));
	}

}
