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
	public String toString() {
		return getToken() + getOperation() + "\n"; //$NON-NLS-1$
	}

}
