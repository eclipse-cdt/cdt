/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

/**
 * Represents the status of a C/C++ debug model element.
 */
public interface ICDebugElementStatus {

	/**
	 * Status severity constant (value 0) indicating this status represents
	 * the nominal case.
	 */
	public static final int OK = 0;

	/**
	 * Status severity constant (value 1) indicating indicating this status
	 * represents a warning.
	 */
	public static final int WARNING = 1;

	/**
	 * Status severity constant (value 2) indicating indicating this status
	 * represents an error.
	 */
	public static final int ERROR = 2;

	/**
	 * Returns whether this status indicates everything is okay
	 * (neither warning, nor error).
	 *
	 * @return <code>true</code> if this status has severity
	 * <code>OK</code>, and <code>false</code> otherwise
	 */
	boolean isOK();

	/**
	 * Returns the severity. The severities are as follows (in descending order):
	 * <ul>
	 * <li><code>ERROR</code> - an error</li>
	 * <li><code>WARNING</code> - a warning</li>
	 * <li><code>OK</code> - everything is just fine</li>
	 * </ul>
	 *
	 * @return the severity: one of <code>OK</code>, <code>ERROR</code>,
	 * or <code>WARNING</code>
	 */
	int getSeverity();

	/**
	 * Returns the message describing the outcome.
	 *
	 * @return a message
	 */
	String getMessage();
}
