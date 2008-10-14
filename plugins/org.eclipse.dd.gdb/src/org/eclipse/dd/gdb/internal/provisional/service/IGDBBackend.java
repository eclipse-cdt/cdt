/*******************************************************************************
 * Copyright (c) 2008 Nokia Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Wang (Nokia) - initial version. Sep, 2008
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.provisional.service;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dd.mi.service.IMIBackend;

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
	 * Return the command line that is used to launch GDB. <br>
	 * Note it's not needed to put debugged binary in the command, as that will
	 * be set by a MI command.
	 *  
	 * @return
	 */
	public List<String> getGDBCommandLine();

	/**
	 * Get path of the debugged program on host.
	 * 
	 * @return IPath
	 */
	public IPath getProgramPath();

	/**
	 * Get init file for GDB.
	 * 
	 * @return file name, may have relative or absolute path, or empty string
	 *         ("") indicating an init file is not specified.
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
	 * Sends an interrupt signal to the GDB process.
	 */
	public void interrupt();
}
