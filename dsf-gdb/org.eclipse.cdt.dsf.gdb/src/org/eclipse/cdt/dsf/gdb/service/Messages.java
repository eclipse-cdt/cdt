/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 235747
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.osgi.util.NLS;

/**
 * Preference strings.
 * @since 4.1
 */
class Messages extends NLS {
	public static String Already_connected_process_err;
	public static String Tracing_not_supported_error;
	public static String Invalid_post_mortem_type_error;
	public static String Cannot_get_post_mortem_file_path_error;
	public static String GroupPattern;
	public static String NoMatches;
	public static String UniqueMatch;
	public static String UniqueMatches;
	public static String ErrorNotSupported;
	public static String RegisterGroup_name_reserved;
	public static String RegisterGroup_name_used;
	public static String RegisterGroup_invalid_number_of_registers;
	public static String GDB_Version_Mismatch;
	public static String PTY_Console_not_available;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
