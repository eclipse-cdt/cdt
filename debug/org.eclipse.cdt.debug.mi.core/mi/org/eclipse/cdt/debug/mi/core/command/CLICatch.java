/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.CLICatchInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;



/**
 *  Gdb catch command
 */
public class CLICatch extends CLICommand {

	MIOutput out;

	public CLICatch(String event, String arg) {
		super("catch " + event + ' ' +arg); //$NON-NLS-1$
	}

	/**
	 * This command return breakpoint inserted
	 */
	@Override
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new CLICatchInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

}
