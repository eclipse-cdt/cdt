/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Abeer Bagul (Tensilica) - Updated error message (Bug 339048)
 *     Jason Litton (Sage Electronic Engineering, LLC) - Added support for dynamic tracing option (Bug 379169)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Each memory context needs a different MemoryRetrieval (Bug 250323)
 *******************************************************************************/
package org.eclipse.cdt.llvm.dsf.lldb.core.internal;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LLDBCorePlugin extends Plugin {

	/**
	 * LLDB Core Plug-in ID
	 */
	public static final String PLUGIN_ID = "org.eclipse.cdt.llvm.dsf.lldb.core"; //$NON-NLS-1$
	private static LLDBCorePlugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LLDBCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Creates an IStatus for this plug-in using a message.
	 *
	 * @param msg
	 *            the message
	 * @return an IStatus
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static IStatus createStatus(String msg) {
		return createStatus(msg, null);
	}

	/**
	 * Creates an IStatus for this plug-in using a message and exception.
	 *
	 * @param msg
	 *            the message
	 * @param e
	 *            the exception
	 * @return an IStatus
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
	}

	/**
	 * Logs an IStatus
	 *
	 * @param status the IStatus
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Logs a messages with exception.
	 *
	 * @param message
	 *            the message
	 * @param e
	 *            the exception
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message, Throwable e) {
		Throwable nestedException;
		if (e instanceof CModelException && (nestedException = ((CModelException) e).getException()) != null) {
			e = nestedException;
		}
		log(createStatus(message, e));
	}

	/**
	 * Logs an exception.
	 *
	 * @param e
	 *            the exception
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(Throwable e) {
		if (e instanceof CoreException) {
			log(((CoreException) e).getStatus());
		} else {
			String msg = e.getMessage();
			if (msg == null) {
				log("Error", e); //$NON-NLS-1$
			} else {
				log("Error: " + msg, e); //$NON-NLS-1$
			}
		}
	}
}
