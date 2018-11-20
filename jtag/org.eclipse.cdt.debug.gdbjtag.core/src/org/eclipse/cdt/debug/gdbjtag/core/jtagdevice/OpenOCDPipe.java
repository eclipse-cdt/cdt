/*******************************************************************************
 * Copyright (c) 2008, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     John Dallaway - OpenOCD extensions, bug 494059
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.jtagdevice;

import java.util.Collection;

/**
 * @since 9.2
 */
public class OpenOCDPipe extends DefaultGDBJtagConnectionImpl {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagConnectionImpl#doDelay(int, java.util.Collection)
	 */
	@Override
	public void doDelay(int delay, Collection<String> commands) {
		addCmd(commands, "monitor sleep " + String.valueOf(delay * 1000)); //$NON-NLS-1$
	}

}
