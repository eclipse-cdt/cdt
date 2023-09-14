/*******************************************************************************
 * Copyright (c) 2008, 2023 QNX Software Systems and others.
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
 *     John Dallaway - NXP LinkServer extension (#496)
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.jtagdevice;

import java.util.Collection;

/**
 * @since 10.8
 */
public class NXPLinkServer extends DefaultGDBJtagConnectionImpl {

	@Override
	public void doHalt(Collection<String> commands) {
		/* not supported */
	}

	@Override
	public void doReset(Collection<String> commands) {
		doResetAndHalt(commands);
		doContinue(commands);
	}

	@Override
	public void doResetAndHalt(Collection<String> commands) {
		addCmd(commands, "monitor reset"); //$NON-NLS-1$
	}

}
