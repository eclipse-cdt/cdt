/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *
 * @since 4.0
 */
public interface IProcessExtendedInfo {
	/**
	 * Returns the pid of the process, as assigned by the OS.
	 */
	public int getPid();

	/**
	 * Returns the name of the process, as assigned by the OS.
	 * Returns null if that information is not available.
	 */
	public String getName();

	/**
	 * Returns a list of cores on which the process is located.
	 * This list is all cores on which at least one thread of the
	 * process is located.
	 * Returns null if that information is not available.
	 */
	public String[] getCores();

	/**
	 * Returns the owner of the process.  Usually the userId
	 * that started the process.  Returns null if that
	 * information is not available.
	 */
	public String getOwner();

	/**
	 * @return The description of this process, i.e. the program and
	 *         its arguments, or null if not available.
	 * @since 5.6
	 */
	public String getDescription();
}
