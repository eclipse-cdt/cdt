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

}
