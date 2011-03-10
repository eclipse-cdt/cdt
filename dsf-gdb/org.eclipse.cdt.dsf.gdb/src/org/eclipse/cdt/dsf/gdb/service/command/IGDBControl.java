/*******************************************************************************
 * Copyright (c) 2008, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse  License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Vladimir Prus (CodeSourcery) - Support for -data-read-memory-bytes (bug 322658)     
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.AbstractCLIProcess;

public interface IGDBControl extends IMICommandControl {

	AbstractCLIProcess getCLIProcess();

	/**
	 * Request to terminate GDB.
	 * 
	 * @param rm The requestMonitor indicating that GDB has been terminated.
	 */
	void terminate(RequestMonitor rm);
	
	/**
	 * This method should be called once and only once, during the launch,
	 * to complete the initialization.  It will perform the final steps
	 * to configure GDB for the type of debugging session chosen by the
	 * user.
	 * 
	 * @param rm The requestMonitor indicating that the final steps if 
	 *           initialization are completed.
	 *                       
	 * @since 4.0
	 */
	void completeInitialization(RequestMonitor rm);
	
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
	
	/**
	 * Enable the pretty printers also for MI variable objects. This basically
	 * sends -enable-pretty-printing.
	 * 
	 * @param rm
	 * 
	 * @since 4.0
	 */
	void enablePrettyPrintingForMIVariableObjects(RequestMonitor rm);

	/**
	 * Turns the printing of python errors on or off.
	 * 
	 * @param enabled
	 *            If <code>true</code>, printing errors is turned on.
	 * @param rm
	 * 
	 * @since 4.0
	 */
	void setPrintPythonErrors(boolean enabled, RequestMonitor rm);
}