/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
* QNX Software Systems - catchpoints - bug 226689
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * Interface for debugger event breakpoints. Example of event breakpoint
 * is break on raising exception in C++, or break on receiving signal.
 * 
 * @sinse 5.0
 * @since 7.0
 */
public interface ICEventBreakpoint extends ICBreakpoint {
	/**
	 * Breakpoint attribute storing the event breakpoint event id. Basically,
	 * this indicates what type of event the breakpoint catches--e.g., a C++
	 * exception throw, a library load, a thread exit, etc. Event types can be
	 * contributed via the "breakpointContribution" extension point. Stock CDT
	 * contributes a number of them, which are represented here by the
	 * EVENT_TYPE_XXXXX constants
	 * 
	 * <p>
	 * This attribute is a <code>String</code>.
	 * 
	 */
	public static final String EVENT_TYPE_ID = "org.eclipse.cdt.debug.core.eventbreakpoint_event_id"; //$NON-NLS-1$

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
	 * catches a signal (POSIX). This type of event has a single parameter of
	 * type in, indicating the specific signal.
	 * 
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_SIGNAL_CATCH = "org.eclipse.cdt.debug.gdb.signal"; //$NON-NLS-1$

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
	 * calls exit() (POSIX). This type of event has no parameters.
	 * 
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_EXIT = "org.eclipse.cdt.debug.gdb.catch_exit"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when a new
	 * process starts. This type of event has no parameters.
	 * 
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_PROCESS_START = "org.eclipse.cdt.debug.gdb.catch_start"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when a
	 * process exits. This type of event has no parameters.
	 * 
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_PROCESS_STOP = "org.eclipse.cdt.debug.gdb.catch_stop"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when a new
	 * thread starts. This type of event has no parameters.
	 * 
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_THREAD_START = "org.eclipse.cdt.debug.gdb.catch_thread_start"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when a
	 * thread exits. This type of event has no parameters.
	 * 
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_THREAD_EXIT = "org.eclipse.cdt.debug.gdb.catch_thread_exit"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when a
	 * thread joins another one (waits for it to exit) This type of event has no
	 * parameters.
	 * 
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_THREAD_JOIN = "org.eclipse.cdt.debug.gdb.catch_thread_join"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when it
	 * loads a library. This type of event has no parameters.
	 * 
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_LIBRARY_LOAD = "org.eclipse.cdt.debug.gdb.catch_load"; //$NON-NLS-1$

	/**
	 * An event breakpoint of this type suspends the target program when it
	 * unloads a library. This type of event has no parameters.
	 * 
	 * @since 7.0
	 */
	public static final String EVENT_TYPE_LIBRARY_UNLOAD = "org.eclipse.cdt.debug.gdb.catch_unload"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing the event breakpoint argument. This
	 * attribute is a <code>String</code>, though it may be a stringified
	 * representation of another type (it may be a number, for example).
	 * Currently, an event type can have only one argument
	 */
	public static final String EVENT_ARG = "org.eclipse.cdt.debug.core.eventbreakpoint_event_arg"; //$NON-NLS-1$	

	/**
	 * Get the event breakpoint type. One of our EVENT_TYPE_XXXXX constants or a
	 * custom type contributed via the <code>breakpointContribution</code>
	 * extension point
	 * 
	 * @return event breakpoint type id (not null)
	 * @throws CoreException
	 */
	String getEventType() throws CoreException;

	/**
	 * Get the event argument, if the type has one. Currently, an event type can
	 * have at most one argument.
	 * 
	 * @return event argument, or null if not applicable
	 * @throws CoreException
	 */
	String getEventArgument() throws CoreException;
}
