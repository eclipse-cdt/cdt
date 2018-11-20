/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

/**
 * Defines status codes relevant to the CDT UI plug-in. When a
 * Core exception is thrown, it contain a status object describing
 * the cause of the exception. The status objects originating from the
 * CDT UI plug-in use the codes defined in this interface.
  */
public interface ICStatusConstants {

	// C UI status constants start at 10000 to make sure that we don't
	// collide with resource and c model constants.

	public static final int INTERNAL_ERROR = 10001;

	/**
	 * Status constant indicating that an exception occurred on
	 * storing or loading templates.
	 */
	public static final int TEMPLATE_IO_EXCEPTION = 10002;

	/**
	 * Status constant indicating that an validateEdit call has changed the
	 * content of a file on disk.
	 */
	public static final int VALIDATE_EDIT_CHANGED_CONTENT = 10003;

	/**
	 * Status constant indicating that a <tt>ChangeAbortException</tt> has been
	 * caught.
	 */
	public static final int CHANGE_ABORTED = 10004;

	/**
	 * Status constant indicating that an exception occurred while
	 * parsing template file.
	 */
	public static final int TEMPLATE_PARSE_EXCEPTION = 10005;

	/**
	 * Status constant indication that a problem occurred while calculating
	 * the changed region during a save.
	 *
	 * @since 5.1
	 */
	public static final int EDITOR_CHANGED_REGION_CALCULATION = 10006;
}
