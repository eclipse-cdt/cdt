/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Leo Hippelainen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.ui;

import org.eclipse.cdt.managedbuilder.core.IManagedIsToolChainSupported;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.osgi.framework.Version;

/**
 * Based on MingwIsToolChainSupported.
 *  
 */
public class LlvmIsToolChainSupported implements IManagedIsToolChainSupported {

	private final boolean supported;
	
	/**
	 * Constructor.
	 * LLVM Toolchain is supported if binary path for LLVM Tools can be found.
	 */
	public LlvmIsToolChainSupported() {
		// Only supported if we can find the llvm tools.
		this.supported = LlvmEnvironmentVariableSupplier.getBinPath() != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedIsToolChainSupported#isSupported(org.eclipse.cdt.managedbuilder.core.IToolChain,
	 * org.osgi.framework.Version, java.lang.String)
	 */
	/**
	 * Return a boolean value that informs if the LLVM Toolchain is supported.
	 */
	@Override
	public boolean isSupported(IToolChain toolChain,
			/*
			 * Version is supported from CDT 7.1.0.
			 * Use org.osgi.framework.PluginVersionIdentifier with older CDT versions.
			 */
			Version version, String instance) {
		return this.supported;
	}

}
