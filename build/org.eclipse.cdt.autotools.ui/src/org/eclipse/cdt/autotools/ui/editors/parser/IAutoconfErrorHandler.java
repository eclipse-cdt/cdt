/*******************************************************************************
 * Copyright (c) 2008, 2012 Nokia Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ed Swartz (Nokia) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors.parser;

/**
 * Clients implement this interface to handle errors encountered while parsing.
 * @author eswartz
 *
 */
public interface IAutoconfErrorHandler {

	/**
	 * Handle an exception associated with the given element
	 * @param exception the exception to handle; has a line number
	 * at the time of call
	 */
	void handleError(ParseException exception);
}
