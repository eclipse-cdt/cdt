/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

public class ProcessFailureException extends Exception {
	private static final long serialVersionUID = 1766239661286962870L;
	private List<IStatus> statuses;

	/**
	 * Constructor based on the message.
	 * @param msg
	 */
	public ProcessFailureException(String msg) {
		super(msg);
	}

	/**
	 * Constructor based on the message and cause.
	 * @param cause
	 */
	public ProcessFailureException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor based on the message and cause.
	 * @param msg
	 * @param cause
	 */
	public ProcessFailureException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor based on the message and causes.
	 * @param msg
	 * @param statuses
	 */
	public ProcessFailureException(String msg, List<IStatus> statuses) {
		super(msg);
		this.statuses = statuses;
	}

	public ProcessFailureException(String msg, Throwable cause, List<IStatus> statuses) {
		super(msg, cause);
		this.statuses = statuses;
	}

	/**
	 * Returns the Statuses.
	 * @return   List, contains the IStatus.
	 */
	public List<IStatus> getStatuses() {
		return statuses;
	}
}
