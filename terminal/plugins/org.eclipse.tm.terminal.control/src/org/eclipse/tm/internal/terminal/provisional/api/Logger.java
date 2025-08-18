/*******************************************************************************
 * Copyright (c) 2005, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Fran Litterio (Wind River) - initial API and implementation
 * Ted Williams (Wind River) - refactored into org.eclipse namespace
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.StackWalker.StackFrame;
import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.internal.terminal.control.impl.TerminalPlugin;

/**
 * A simple logger class. Every method in this class is static, so they can be
 * called from both class and instance methods. To use this class, write code
 * like this:
 * <p>
 *
 * <pre>
 * Logger.log(&quot;something has happened&quot;);
 * Logger.log(&quot;counter is &quot; + counter);
 * </pre>
 *
 * @author Fran Litterio <francis.litterio@windriver.com>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 */
public final class Logger {
	public static final String TRACE_DEBUG_LOG = "org.eclipse.tm.terminal.control/debug/log"; //$NON-NLS-1$
	public static final String TRACE_DEBUG_LOG_CHAR = "org.eclipse.tm.terminal.control/debug/log/char"; //$NON-NLS-1$
	public static final String TRACE_DEBUG_LOG_VT100BACKEND = "org.eclipse.tm.terminal.control/debug/log/VT100Backend"; //$NON-NLS-1$
	/**	@since 5.2 */
	public static final String TRACE_DEBUG_LOG_HOVER = "org.eclipse.tm.terminal.control/debug/log/hover"; //$NON-NLS-1$

	private static PrintStream logStream;
	private static StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	private static boolean underTest = false;

	/**
	 * When underTest we want exception that are deep inside the code to be surfaced to the test
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void setUnderTest(boolean underTest) {
		Logger.underTest = underTest;
	}

	static {
		// Any of the known debugging options turns on the creation of the log file
		boolean createLogFile = TerminalPlugin.isOptionEnabled(TRACE_DEBUG_LOG)
				|| TerminalPlugin.isOptionEnabled(TRACE_DEBUG_LOG_CHAR)
				|| TerminalPlugin.isOptionEnabled(TRACE_DEBUG_LOG_VT100BACKEND)
				|| TerminalPlugin.isOptionEnabled(TRACE_DEBUG_LOG_HOVER);

		// Log only if tracing is enabled
		if (createLogFile && TerminalPlugin.getDefault() != null) {
			IPath logFile = Platform.getStateLocation(TerminalPlugin.getDefault().getBundle());
			if (logFile != null && logFile.toFile().isDirectory()) {
				logFile = logFile.append("tmterminal.log"); //$NON-NLS-1$
				try {
					logStream = new PrintStream(new FileOutputStream(logFile.toFile(), true));
				} catch (Exception ex) {
					logStream = System.err;
					logStream.println("Exception when opening log file -- logging to stderr!"); //$NON-NLS-1$
					ex.printStackTrace(logStream);
				}
			}
		}
	}

	/**
	 * Encodes a String such that non-printable control characters are
	 * converted into user-readable escape sequences for logging.
	 * @param message String to encode
	 * @return encoded String
	 */
	public static final String encode(String message) {
		boolean encoded = false;
		StringBuffer buf = new StringBuffer(message.length() + 32);
		for (int i = 0; i < message.length(); i++) {
			char c = message.charAt(i);
			switch (c) {
			case '\\':
			case '\'':
				buf.append('\\');
				buf.append(c);
				encoded = true;
				break;
			case '\r':
				buf.append('\\');
				buf.append('r');
				encoded = true;
				break;
			case '\n':
				buf.append('\\');
				buf.append('n');
				encoded = true;
				break;
			case '\t':
				buf.append('\\');
				buf.append('t');
				encoded = true;
				break;
			case '\f':
				buf.append('\\');
				buf.append('f');
				encoded = true;
				break;
			case '\b':
				buf.append('\\');
				buf.append('b');
				encoded = true;
				break;
			default:
				if (c <= '\u000f') {
					buf.append('\\');
					buf.append('x');
					buf.append('0');
					buf.append(Integer.toHexString(c));
					encoded = true;
				} else if (c >= ' ' && c < '\u007f') {
					buf.append(c);
				} else if (c <= '\u00ff') {
					buf.append('\\');
					buf.append('x');
					buf.append(Integer.toHexString(c));
					encoded = true;
				} else {
					buf.append('\\');
					buf.append('u');
					if (c <= '\u0fff') {
						buf.append('0');
					}
					buf.append(Integer.toHexString(c));
					encoded = true;
				}
			}
		}
		if (encoded) {
			return buf.toString();
		}
		return message;
	}

	/**
	 * Checks if logging is enabled.
	 * @return true if logging is enabled.
	 */
	public static final boolean isLogEnabled() {
		return (logStream != null);
	}

	/**
	 * Logs the specified message. Do not append a newline to parameter
	 * <i>message</i>. This method does that for you.
	 *
	 * Does not write to the message to the Eclipse log
	 *
	 * @param message           A String containing the message to log.
	 */
	public static final void log(String message) {
		if (logStream != null) {
			logStream.println(getCallSiteDescription() + ": " + message); //$NON-NLS-1$
			logStream.flush();
		}
	}

	/**
	 * Writes an error message to the Terminal log and the Eclipse log
	 * @since 5.2
	 */
	public static final void logError(String message) {
		logStatus(new Status(IStatus.ERROR, TerminalPlugin.PLUGIN_ID, IStatus.OK, message, null));
	}

	/**
	 * Writes an exception to the Terminal log and the Eclipse log
	 */
	public static final void logException(Exception ex) {
		if (underTest) {
			throw new RuntimeException("Terminal Under Test - examine cause for real failure", ex); //$NON-NLS-1$
		}
		logStatus(new Status(IStatus.ERROR, TerminalPlugin.PLUGIN_ID, IStatus.OK, ex.getMessage(), ex));
	}

	/**
	 * Writes a Status to the Terminal log and the Eclipse log
	 * @since 5.2
	 */
	public static final void logStatus(IStatus status) {
		// log in eclipse error log
		if (TerminalPlugin.getDefault() != null) {
			TerminalPlugin.getDefault().getLog().log(status);
		} else {
			System.err.println(status);
			if (status.getException() != null) {
				status.getException().printStackTrace();
			}
		}
		// Additional Tracing for debug purposes:
		// Read my own stack to get the class name, method name, and line number
		// of where this method was called
		if (logStream != null) {
			logStream.println(getCallSiteDescription() + ": " + //$NON-NLS-1$
					status);
			if (status.getException() != null) {
				status.getException().printStackTrace(logStream);
			}
		}
	}

	/**
	 * Return a description string of the call site of this logging call for use in logged messages.
	 * This method will walk the stack to find the first method in the call stack not from the Logger
	 * class.
	 */
	private static String getCallSiteDescription() {
		Optional<StackFrame> stackFrame = walker
				.walk(stream -> stream.filter(f -> f.getDeclaringClass() != Logger.class).findFirst());
		int lineNumber = stackFrame.map(StackFrame::getLineNumber).orElse(0);
		String className = stackFrame.map(StackFrame::getDeclaringClass).map(Class::getName)
				.map(name -> name.substring(name.lastIndexOf('.') + 1)).orElse("UnknownClass"); //$NON-NLS-1$
		String methodName = stackFrame.map(StackFrame::getMethodName).orElse("unknownMethod"); //$NON-NLS-1$
		String locationString = className + "." + methodName + ":" + lineNumber; //$NON-NLS-1$//$NON-NLS-2$
		return locationString;
	}
}
