/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core;

import org.eclipse.osgi.util.NLS;

/**
 * @author weber
 *
 */
/* package */ public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.cmake.is.core.messages"; //$NON-NLS-1$
	public static String CompileCommandsJsonParser_errmsg_empty_json;
	public static String CompileCommandsJsonParser_errmsg_file_not_found;
	public static String CompileCommandsJsonParser_errmsg_no_parser_for_commandline;
	public static String CompileCommandsJsonParser_errmsg_not_json;
	public static String CompileCommandsJsonParser_errmsg_read_error;
	public static String CompileCommandsJsonParser_errmsg_unexpected_json;
	public static String CompileCommandsJsonParser_msg_detecting_builtins;
	public static String CompileCommandsJsonParser_msg_processing;
	public static String CompileCommandsJsonParser_MSG_WORKBENCH_WILL_NOT_KNOW;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
