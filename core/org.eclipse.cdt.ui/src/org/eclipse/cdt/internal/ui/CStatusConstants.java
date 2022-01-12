/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

/**
 * Defines status codes relevant to the C UI plug-in. When a
 * Core exception is thrown, it contain a status object describing
 * the cause of the exception. The status objects originating from the
 * C UI plug-in use the codes defined in this interface.
 */
public class CStatusConstants {

	// Prevent instantiation
	private CStatusConstants() {
	}

	/** Status code describing an internal error */
	public static final int INTERNAL_ERROR = 1;

	/**
	 * Status constant indicating that an exception occured on
	 * storing or loading templates.
	 */
	public static final int TEMPLATE_IO_EXCEPTION = 2;

}
