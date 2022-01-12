/*******************************************************************************
 * Copyright (c) 2005, 2013 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Andrew Gvozdev    - Ability to use different Cygwin versions in different configurations
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.gnu.cygwin;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.internal.core.Cygwin;
import org.eclipse.cdt.managedbuilder.core.IManagedIsToolChainSupported;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableManagerToolChain;
import org.osgi.framework.Version;

/**
 * This class implements the IManagedIsToolChainSupported for the Gnu Cygwin tool-chain
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class IsGnuCygwinToolChainSupported implements IManagedIsToolChainSupported {
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$

	@Override
	public boolean isSupported(IToolChain toolChain, Version version, String instance) {
		IEnvironmentVariable var = new EnvironmentVariableManagerToolChain(toolChain).getVariable(ENV_PATH, true);
		String envPath = var != null ? var.getValue() : null;
		return Cygwin.isAvailable(envPath);
	}
}
