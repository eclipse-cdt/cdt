/**********************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.gnu.mingw;

import org.eclipse.cdt.managedbuilder.core.IManagedIsToolChainSupported;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.osgi.framework.Version;

/**
 * @author Doug Schaefer
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MingwIsToolChainSupported implements IManagedIsToolChainSupported {

	private final boolean supported;

	public MingwIsToolChainSupported() {
		// Only supported if we can find the mingw bin dir to run the compiler
		supported = MingwEnvironmentVariableSupplier.getBinDir() != null;
	}

	@Override
	public boolean isSupported(IToolChain toolChain, Version version, String instance) {
		return supported;
	}

}
