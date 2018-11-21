/*******************************************************************************
 * Copyright (c) 2009, 2012 Nokia Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Nokia - initial version. May 5, 2009
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.command.MIBackendCLIProcess;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Note that starting with GDB 7.12, as long as a PTY is available,
 * this process is no longer used.  Instead, the real GDB process,
 * along with its console will be used directly.  A second PTY
 * will be used to communicate using MI.

 * @author LWang
 * @since 2.0
 *
 */
public class GDBBackendCLIProcess extends MIBackendCLIProcess {

	/**
	* @param commandControl
	* @param backend
	* @throws IOException
	*/
	public GDBBackendCLIProcess(ICommandControlService commandControl, IMIBackend backend) throws IOException {
		super(commandControl, backend);
		assert (commandControl instanceof IGDBControl);
	}

	@Override
	public void destroy() {
		try {
			// This is called when user terminate the "launch" or "gdb" process
			// in Debug View. We need to kill inferior too. Fix bug
			//   https://bugs.eclipse.org/bugs/show_bug.cgi?id=234467
			//
			getSession().getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					if (!DsfSession.isSessionActive(getSession().getId()))
						return;
					if (isDisposed())
						return;

					((IGDBControl) getCommandControlService())
							.terminate(new RequestMonitor(getSession().getExecutor(), null));
				}
			});
		} catch (RejectedExecutionException e) {
			// Session disposed.
		}
	}

	/**
	* @since 3.0
	*/
	@Override
	protected boolean isMissingSecondaryPromptCommand(String operation) {
		// The 'actions' command does not get a secondary prompt!
		if (operation.startsWith("ac") && "actions".indexOf(operation) != -1) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
		return false;
	}
}
