/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Alvaro Sanchez-Leon (Ericsson) - Support Register Groups (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.osgi.util.NLS;

/**
 * Preference strings.
 * @since 3.0
 */
class Messages extends NLS {
	public static String Breakpoint_attribute_detailed_problem;
	public static String Breakpoint_attribute_problem;
	public static String Breakpoint_installation_failed;
	public static String MIExpressions_NotAvailableBecauseChildOfDynamicVarobj;
	public static String MIExpressions_ReturnValueAlias;
	public static String MIRegisters_General_Registers;
	public static String MIRegisters_General_Registers_description;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
