/*******************************************************************************
 * Copyright (c) 2008, 2012 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.AbstractCLIProcess;

public interface IGDBControl extends IMICommandControl {

	/**
	 * Returns the process that represents GDB.
	 * This is the process that should be added to the launch.
	 * Note that this is usually not be the actual GDB process but
	 * only one that is used to represent it.
	 * To get the real GDB process use
	 * {@link IGDBBackend#getProcess()}.
	 *
	 * @since 5.2
	 */
	default Process getGDBBackendProcess() {
		return getCLIProcess();
	}

	/**
	 * @deprecated The return value of this method was too
	 *             restrictive.  It has been replaced with
	 *             {@link #getGDBBackendProcess()}
	 * @return The AbstractCLIProcess that handles the CLI.
	 *         Will return null if the CLI is not handled
	 *         by an AbstractCLIProcess; this will sometimes
	 *         happen for GDB >= 7.12 if the CLI is handled
	 *         by the GDB process itself.
	 */
	@Deprecated
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