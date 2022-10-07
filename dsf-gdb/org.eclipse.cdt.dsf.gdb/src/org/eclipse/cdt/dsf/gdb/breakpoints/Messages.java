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

/**
 * @noreference This class is not intended to be referenced by clients.
 */
public class Messages extends NLS {
	public static String DynamicPrintf_Invalid_string;
	public static String DynamicPrintf_Printf_must_start_with_quote;
	public static String DynamicPrintf_Printf_missing_closing_quote;
	public static String DynamicPrintf_Missing_comma;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
