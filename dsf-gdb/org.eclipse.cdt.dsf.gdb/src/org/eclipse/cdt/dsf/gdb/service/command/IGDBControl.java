/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse  License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Vladimir Prus (CodeSourcery) - Support for -data-read-memory-bytes (bug 322658)     
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.AbstractCLIProcess;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess;

public interface IGDBControl extends IMICommandControl {

	void terminate(final RequestMonitor rm);
	void initInferiorInputOutput(final RequestMonitor requestMonitor);

	boolean canRestart();
	void start(GdbLaunch launch, final RequestMonitor requestMonitor);
	void restart(final GdbLaunch launch, final RequestMonitor requestMonitor);
	void createInferiorProcess();

	boolean isConnected();

	void setConnected(boolean connected);

	AbstractCLIProcess getCLIProcess();

	MIInferiorProcess getInferiorProcess();

	/**
	 * @since 2.0
	 */
	void setTracingStream(OutputStream tracingStream);
	
	/** 
	 * Sets any user-defined environment variables for the inferior.
	 * 
	 * If the 'clear' flag is true, all existing environment variables
	 * will be removed and replaced with the new specified ones. 
	 * If 'clear' is false, the new variables are added to the existing
	 * environment.
	 * 
	 * @since 3.0 
	 */
	void setEnvironment(Properties props, boolean clear, RequestMonitor requestMonitor);
	
	/**
	 * Returns a list of all the target-independent MI features
	 * supported by the GDB that is being used. Consult the GDB MI documentation
	 * for the MI -list-features command for the possible names of features.
	 * 
	 * The return value is never null but may be an empty list. 
	 * 
	 * @since 4.0
	 */
	List<String> getFeatures();
}