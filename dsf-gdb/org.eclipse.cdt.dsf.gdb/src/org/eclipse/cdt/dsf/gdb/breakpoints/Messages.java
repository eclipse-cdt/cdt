/*******************************************************************************
  * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Support for dynamic printf (bug 400628)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.breakpoints;

import org.eclipse.osgi.util.NLS;

/** @since 4.4 */
public class Messages extends NLS {
	public static String DynamicPrintf_Invalid_string;
	public static String DynamicPrintf_Printf_must_start_with_quote;
	public static String DynamicPrintf_Printf_missing_closing_quote;
	/**
	 * @since 5.3
	 */
	public static String DynamicPrintf_Printf_not_expecting_a_closing_parenthesis;
	public static String DynamicPrintf_Missing_comma;
	public static String DynamicPrintf_Empty_arg;
	public static String DynamicPrintf_Extra_arg;
	public static String DynamicPrintf_Missing_arg;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
