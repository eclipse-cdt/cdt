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
 * Clients implement this interface to validate macro calls.
 * @author eswartz
 *
 */
public interface IAutoconfMacroValidator {

	/**
	 * Validate the given macro call.
	 * @param element macro call, never <code>null</code>
	 * @throws ParseException if the call doesn't match the expected number of elements 
	 */
	void validateMacroCall(AutoconfMacroElement element) throws ParseException, InvalidMacroException;
}
