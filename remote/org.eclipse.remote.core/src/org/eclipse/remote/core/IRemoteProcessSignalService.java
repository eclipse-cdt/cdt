/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import org.eclipse.remote.core.exception.RemoteConnectionException;

/**
 * A service abstraction for signals.
 *
 * @since 2.0
 */
public interface IRemoteProcessSignalService extends IRemoteProcess.Service {
	public static final int HUP = 1;
	public static final int INT = 2;
	public static final int QUIT = 3;
	public static final int ILL = 4;
	public static final int ABRT = 6;
	public static final int FPE = 8;
	public static final int KILL = 9;
	public static final int SEGV = 11;
	public static final int PIPE = 13;
	public static final int ALRM = 14;
	public static final int TERM = 15;
	public static final int STOP = 17;
	public static final int TSTP = 18;
	public static final int CONT = 19;
	public static final int USR1 = 30;
	public static final int USR2 = 31;

	/**
	 * Send a signal to the remote process.
	 *
	 * @param signal
	 *            signal to send.
	 * @throws RemoteConnectionException
	 *             if the underlying connection fails
	 * @since 2.0
	 */
	void sendSignal(int signal) throws RemoteConnectionException;
}
