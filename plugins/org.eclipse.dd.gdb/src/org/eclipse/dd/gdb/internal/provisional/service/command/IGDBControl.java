/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse  License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.provisional.service.command;

import org.eclipse.core.runtime.IPath;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService;
import org.eclipse.dd.gdb.internal.provisional.launching.GdbLaunch;
import org.eclipse.dd.gdb.internal.provisional.service.SessionType;
import org.eclipse.dd.mi.service.command.AbstractCLIProcess;
import org.eclipse.dd.mi.service.command.MIInferiorProcess;

public interface IGDBControl extends ICommandControlService {

	SessionType getSessionType();

	boolean getIsAttachSession();
	boolean canInterrupt();

	void interrupt();

	void destroy();

	void terminate(final RequestMonitor rm);
	void initInferiorInputOutput(final RequestMonitor requestMonitor);

	boolean canRestart();
	void start(GdbLaunch launch, final RequestMonitor requestMonitor);
	void restart(final GdbLaunch launch, final RequestMonitor requestMonitor);
	void createInferiorProcess();

	boolean isConnected();

	void setConnected(boolean connected);

	Process getGDBProcess();

	AbstractCLIProcess getCLIProcess();

	MIInferiorProcess getInferiorProcess();

    public void resetInferior(MIInferiorProcess inferior);
	
	boolean isGDBExited();

	int getGDBExitCode();

	IPath getExecutablePath();

	void getInferiorProcessId(DataRequestMonitor<String> rm);
}