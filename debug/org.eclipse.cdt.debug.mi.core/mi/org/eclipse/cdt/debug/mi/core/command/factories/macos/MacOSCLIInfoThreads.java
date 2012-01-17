/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Marc-Andre Laperle - use -thread-list-ids for mac, fix for bug 294538
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.macos;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.command.CLIInfoThreads;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoThreadsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

class MacOSCLIInfoThreads extends CLIInfoThreads {
	public MacOSCLIInfoThreads() {
		super();
		// with apple-gdb, we use -thread-list-ids as a replacement for info
		// threads
		setOperation("-thread-list-ids"); //$NON-NLS-1$
	}

	// MI doesn't work with a space between the token and the
	// operation, so we override CLICommmand's toString
	@Override
	public String toString() {
		return getToken() + getOperation() + "\n"; //$NON-NLS-1$
	}

	@Override
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MacOsCLIInfoThreadsInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

	@Override
	public CLIInfoThreadsInfo getMIInfoThreadsInfo() throws MIException {
		return (CLIInfoThreadsInfo) getMIInfo();
	}

}
