/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.core.runtime.PluginVersionIdentifier;

/**
 * 
 * @since 3.0
 */
public interface IManagedIsToolChainSupported {
	/**
	 *
	 * @return <code>true</code> if the given tool-chain is supported on the system
	 * otherwise returns <code>false</code>
	 */
	public boolean isSupported(IToolChain toolChain, 
						PluginVersionIdentifier version, 
						String instance);
}
