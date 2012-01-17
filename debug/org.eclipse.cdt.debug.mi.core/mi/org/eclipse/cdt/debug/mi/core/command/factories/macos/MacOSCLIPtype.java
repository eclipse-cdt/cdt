/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.macos;

import org.eclipse.cdt.debug.mi.core.command.CLIPType;

class MacOSCLIPtype extends CLIPType {

	public MacOSCLIPtype(String var) {
		super(var);
		// apple-gdb does not give a ^error response with an invalid CLI command
		// but with -interpreter-exec console it does
		setOperation("-interpreter-exec console \"ptype " + var + "\""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// MI doesn't work with a space between the token and the
	// operation, so we override CLICommmand's toString
	@Override
	public String toString() {
		return getToken() + getOperation() + "\n"; //$NON-NLS-1$
	}

}
