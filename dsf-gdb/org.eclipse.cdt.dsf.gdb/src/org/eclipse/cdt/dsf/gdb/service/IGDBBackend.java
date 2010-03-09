/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia    - initial version
 *    Ericsson - Minor cleanup
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Service that manages back end GDB process, such as launching and monitoring
 * GDB process, managing certain GDB parameters/options. This service makes it
 * easy for debugger implementations to customize the way to start GDB process
 * and convert some parameters if needed. See bug 240092 for more.<br>
 * <br>
 * A base implementation {@link GDBBackend} is provided that should be
 * sufficient for most cases. But if you have special needs, it's recommended to
 * subclass the base implementation. <br>
 * <br>
 * Here are some special cases: <br>
 * Example #1: GDB is usually launched on the host machine where Eclipse is
 * running, but it can also be launched on a remote machine through, say, SSH. <br>
 * Example #2: GDB is usually launched in the host file system, but it can also
 * be launched in a chroot'ed file system such as Scratchbox (see
 * http://www.scratchbox.org)<br>
 *
 * @since 1.1
 */
public interface IGDBBackend extends IMIBackend {

	/**
	 * Get path of the debugged program on host.
	 * 
	 * @return IPath
	 */
	public IPath getProgramPath();

	/**
	 * Get init file for GDB.
	 * 
	 * @return file name, may have relative or absolute path, or empty string ("") 
	 *         indicating an init file is not specified.
	 * @throws CoreException
	 *             - error in getting the option.
	 */
	public String getGDBInitFile() throws CoreException;

	/**
	 * get arguments for the debugged program.
	 * 
	 * @return String
	 * @throws CoreException
	 *             - error in getting the option.
	 */
	public String getProgramArguments() throws CoreException;

	/**
	 * Get working directory for GDB. 
	 * 
	 * @return IPath - null if no meaningful value found.
	 * @throws CoreException
	 *             - if any error occurs.
	 */
	public IPath getGDBWorkingDirectory() throws CoreException;

	/**
	 * @throws CoreException
	 *             - error in getting the option.
	 */
	public List<String> getSharedLibraryPaths() throws CoreException;

	/**
	 * Returns the list of user-specified variables.
	 * If no variables are specified, should return an empty list.
	 * Should not return null.
	 * 
	 *  @since 3.0 
	 */
	public Properties getEnvironmentVariables() throws CoreException;
	
	/** 
	 * Returns whether the native environment should be cleared before
	 * setting the user-specified environment variables.
	 * 
	 * @since 3.0 */
	public boolean getClearEnvironment() throws CoreException;
	
	/**
	 * Sends an interrupt signal to the GDB process.
	 */
	public void interrupt();

	/**
	 * Interrupts the backend.
	 * The request monitor indicates if the interrupt succeeded or not;
	 * it may or may not indicate that the backend is already interrupted,
	 * but does indicate if the backend will become interrupted.
	 * 
	 * @since 3.0
	 */
	public void interrupt(RequestMonitor rm);

	/**
	 * @return The type of the session currently ongoing with the backend
	 */
	public SessionType getSessionType();

	/**
	 * @return true if the ongoing session is attaching to a remote target.
	 */	
	public boolean getIsAttachSession();
}
