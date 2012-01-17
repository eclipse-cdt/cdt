/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoLineInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

public class CLIInfoLine extends CLICommand {

	public CLIInfoLine(IAddress address) {
		super("info line *" + address.toHexAddressString()); //$NON-NLS-1$
	}

	public CLIInfoLineInfo getMIInfoLineInfo() throws MIException {
		return (CLIInfoLineInfo)getMIInfo();
	}

	@Override
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new CLIInfoLineInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

}
