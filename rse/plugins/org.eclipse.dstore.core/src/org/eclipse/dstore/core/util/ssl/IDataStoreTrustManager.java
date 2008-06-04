/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight   (IBM) - [225507][api][breaking] RSE dstore API leaks non-API types
 ********************************************************************************/
package org.eclipse.dstore.core.util.ssl;

import java.util.List;

import javax.net.ssl.X509TrustManager;

/**
 * Extracted interface from DataStoreTrustManager.
 * @since 3.0
 */
public interface IDataStoreTrustManager extends X509TrustManager
{
	/**
	 * Sets the path and password for the trust manager
	 * @param filePath the path
	 * @param password the password
	 */
	public void setKeystore(String filePath, String password);

	/**
	 * Returns the list of untrusted certificates
	 * @return the list of untrusted certificates
	 */
	public List getUntrustedCerts();
}
