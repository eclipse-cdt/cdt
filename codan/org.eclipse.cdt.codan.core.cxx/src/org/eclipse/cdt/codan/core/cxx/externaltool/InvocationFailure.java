/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.externaltool;

/**
 * Indicates that invocation of an external tool failed.
 *
 * @since 2.1
 */
public class InvocationFailure extends Exception {
	private static final long serialVersionUID = 6727101323050538885L;

	/**
	 * Constructor.
	 * @param message the detail message.
	 */
	public InvocationFailure(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message the detail message.
	 * @param cause the cause (which is saved for later retrieval by
	 *        the <code>{@link #getCause()}</code> method.)
	 */
	public InvocationFailure(String message, Throwable cause) {
		super(message, cause);
	}
}
