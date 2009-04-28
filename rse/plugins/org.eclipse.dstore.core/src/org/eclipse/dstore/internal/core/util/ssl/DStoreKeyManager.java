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
 * David McKnight  (IBM)  - [264858][dstore] OpenRSE always picks the first trusted certificate
 ********************************************************************************/
package org.eclipse.dstore.internal.core.util.ssl;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

public class DStoreKeyManager implements X509KeyManager {
	
	private X509KeyManager _keyManager;
	private String _defaultAlias;
	
	public DStoreKeyManager(X509KeyManager keyManager, String defaultAlias){
		_keyManager = keyManager;
		_defaultAlias = defaultAlias;
	}
	
	public String chooseClientAlias(String[] keyType, Principal[] issuers,
			Socket socket) {
		if (_defaultAlias != null){
			return _defaultAlias;
		}
		else {
			return _keyManager.chooseClientAlias(keyType, issuers, socket);
		}
	}

	public String chooseServerAlias(String keyType, Principal[] issuers,
			Socket socket) {
		if (_defaultAlias != null){
			return _defaultAlias;
		}
		else {
			return _keyManager.chooseServerAlias(keyType, issuers, socket);
		}
	}

	public X509Certificate[] getCertificateChain(String alias) {
		return _keyManager.getCertificateChain(alias);
	}

	public String[] getClientAliases(String keyType, Principal[] issuers) {
		return _keyManager.getClientAliases(keyType, issuers);
	}

	public PrivateKey getPrivateKey(String alias) {
		return _keyManager.getPrivateKey(alias);
	}

	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return _keyManager.getServerAliases(keyType, issuers);
	}

}
