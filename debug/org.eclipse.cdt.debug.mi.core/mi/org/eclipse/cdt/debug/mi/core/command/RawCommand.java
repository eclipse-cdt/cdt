/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 */
public class RawCommand extends Command {

	String fRaw;

	public RawCommand(String operation) {
		fRaw = operation;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (fRaw == null) {
			fRaw = "\n"; //$NON-NLS-1$;
		} else if (! fRaw.endsWith("\n")) { //$NON-NLS-1$
			fRaw += "\n"; //$NON-NLS-1$
		}
		return fRaw;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.Command#getMIOutput()
	 */
	public MIOutput getMIOutput() {
		return new MIOutput();
	}
}
