/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [186525] Move keystoreProviders to core
 ********************************************************************************/

package org.eclipse.rse.core.comm;

import java.util.List;

/*
 * Interface for the keystoreProviders extension point.
 * Implementors must provide a keystore and it's password.
 */
public interface ISystemKeystoreProvider
{
	public String getKeyStorePassword();
	public String getKeyStorePath();
	public boolean importCertificates(List certificates, String systemName);
}
