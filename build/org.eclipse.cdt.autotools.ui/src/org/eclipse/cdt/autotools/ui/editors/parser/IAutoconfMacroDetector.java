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
 * Clients implement this interface to identify what identifiers represent
 * macros for the autoconf tree.
 * @author eswartz
 *
 */
public interface IAutoconfMacroDetector {

	/**
	 * Tell if this identifier should be treated as an m4 macro call.
	 * The identifier has already been judged to be a valid candidate
	 * (i.e. it's not quoted).
	 * @param name the string to check
	 */
	boolean isMacroIdentifier(String name);
}
