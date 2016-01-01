/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.osgi.framework.Version;

/**
 * 
 * @since 3.0
 */
public interface IManagedIsToolChainSupported {
	/**
	 * @return <code>true</code> if the given tool-chain is supported on the system
	 * otherwise returns <code>false</code>
	 * 
	 * @since 8.0
	 */
	public boolean isSupported(IToolChain toolChain, Version version, String instance);
}
