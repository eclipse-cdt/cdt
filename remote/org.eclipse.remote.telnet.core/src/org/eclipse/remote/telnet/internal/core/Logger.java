/*******************************************************************************
 * Copyright (c) 2005, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
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
 * Greg Watson (IBM) - Adapted for org.eclipse.remote
 *******************************************************************************/
package org.eclipse.remote.telnet.internal.core;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.osgi.framework.BundleContext;

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
 *
 */
public final class Logger implements DebugOptionsListener {
	public static final String TRACE_DEBUG_LOG = "org.eclipse.remote.telnet.core/debug/log"; //$NON-NLS-1$
	public static final String TRACE_DEBUG_LOG_CHAR = "org.eclipse.remote.telnet.coredebug/log/char"; //$NON-NLS-1$
	public static final String TRACE_DEBUG_LOG_VT100BACKEND = "org.eclipse.remote.telnet.core/debug/log/VT100Backend"; //$NON-NLS-1$

	private static PrintStream logStream;
	private static Logger logger;

	private DebugOptions options;

	public static void configure(BundleContext context) {
		if (logger == null) {
			logger = new Logger(context);
		}
	}

	private Logger(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(DebugOptions.LISTENER_SYMBOLICNAME, Activator.PLUGIN_ID);
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}

	/**
	 * Encodes a String such that non-printable control characters are
	 * converted into user-readable escape sequences for logging.
	 *
	 * @param message
	 *            String to encode
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
	 *
	 * @return true if logging is enabled.
	 */
	public static final boolean isLogEnabled() {
		return (logStream != null);
	}

	/**
	 * Logs the specified message. Do not append a newline to parameter
	 * <i>message</i>. This method does that for you.
	 *
	 * @param message
	 *            A String containing the message to log.
	 */
	public static final void log(String message) {
		if (logStream != null) {
			// Read my own stack to get the class name, method name, and line
			// number of
			// where this method was called.

			StackTraceElement caller = new Throwable().getStackTrace()[1];
			int lineNumber = caller.getLineNumber();
			String className = caller.getClassName();
			String methodName = caller.getMethodName();
			className = className.substring(className.lastIndexOf('.') + 1);

			logStream.println(className + "." + methodName + ":" + lineNumber + ": " + message); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			logStream.flush();
		}
	}

	/**
	 * Writes a stack trace for an exception to both Standard Error and to the
	 * log file.
	 */
	public static final void logException(Exception ex) {
		// log in eclipse error log
		if (Activator.getDefault() != null) {
			Activator.getDefault().getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, ex.getMessage(), ex));
		} else {
			ex.printStackTrace();
		}
		// Additional Tracing for debug purposes:
		// Read my own stack to get the class name, method name, and line number
		// of where this method was called
		if (logStream != null) {
			StackTraceElement caller = new Throwable().getStackTrace()[1];
			int lineNumber = caller.getLineNumber();
			String className = caller.getClassName();
			String methodName = caller.getMethodName();
			className = className.substring(className.lastIndexOf('.') + 1);

			PrintStream tmpStream = System.err;

			if (logStream != null) {
				tmpStream = logStream;
			}

			tmpStream.println(className + "." + methodName + ":" + lineNumber + ": " + //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					"Caught exception: " + ex); //$NON-NLS-1$
			ex.printStackTrace(tmpStream);
		}
	}

	@Override
	public void optionsChanged(DebugOptions options) {
		this.options = options;

		// Any of the three known debugging options turns on the creation of the log file
		boolean createLogFile = isOptionEnabled(TRACE_DEBUG_LOG) || isOptionEnabled(TRACE_DEBUG_LOG_CHAR)
				|| isOptionEnabled(TRACE_DEBUG_LOG_VT100BACKEND);

		// Log only if tracing is enabled
		if (createLogFile && Activator.getDefault() != null) {
			IPath logFile = Platform.getStateLocation(Activator.getDefault().getBundle());
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

	public boolean isOptionEnabled(String option) {
		if (options == null) {
			return false;
		}
		return options.getBooleanOption(Activator.PLUGIN_ID + option, false);
	}
}
