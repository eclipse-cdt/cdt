/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.gdb.eventbkpts;

public interface IEventBreakpointConstants {
	/**
	 * An event breakpoint of this type suspends the target program when it
	 * catches a C++ exception. This type of event has no parameters.
	 *
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_CATCH = "org.eclipse.cdt.debug.gdb.catch"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when it
	 * throws a C++ exception. This type of event has no parameters.
	 *
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_THROW = "org.eclipse.cdt.debug.gdb.throw"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when it
	 * calls fork() (POSIX). This type of event has no parameters.
	 *
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_FORK = "org.eclipse.cdt.debug.gdb.catch_fork"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when it
	 * calls vfork() (POSIX). This type of event has no parameters.
	 *
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_VFORK = "org.eclipse.cdt.debug.gdb.catch_vfork"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when it
	 * calls exec() (POSIX). This type of event has no parameters.
	 *
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_EXEC = "org.eclipse.cdt.debug.gdb.catch_exec"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when it
	 * makes a system call (POSIX). This type of event takes a single parameter:
	 * the name or number of the system call.
	 *
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_SYSCALL = "org.eclipse.cdt.debug.gdb.catch_syscall"; //$NON-NLS-1$
}
