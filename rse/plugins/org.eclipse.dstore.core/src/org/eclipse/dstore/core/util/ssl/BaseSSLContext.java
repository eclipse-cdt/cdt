/********************************************************************************
 * Copyright (c) 2009 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight  (IBM) - [259905][api] provide public API for getting/setting key managers for SSLContext
 ********************************************************************************/

package org.eclipse.dstore.core.util.ssl;

import javax.net.ssl.KeyManager;


/**
 * @since 3.1
 */
public class BaseSSLContext {
	private static KeyManager[] _keyManagers;

	public static void setKeyManagers(KeyManager[] keyManagers){
		_keyManagers = keyManagers;
	}

	public static KeyManager[] getKeyManagers(){
		return _keyManagers;
	}
}
