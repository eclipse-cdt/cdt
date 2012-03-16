/*******************************************************************************
 * Copyright (c) 2008 Nokia Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
