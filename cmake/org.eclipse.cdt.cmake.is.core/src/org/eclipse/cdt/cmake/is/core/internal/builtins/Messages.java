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

package org.eclipse.cdt.cmake.is.core.internal.builtins;

import org.eclipse.osgi.util.NLS;

/**
 * @author weber
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.cmake.is.core.internal.builtins.messages"; //$NON-NLS-1$
	public static String CompilerBuiltinsDetector_errmsg_command_failed;
	public static String CompilerBuiltinsDetector_msg_detection_finished;
	public static String CompilerBuiltinsDetector_msg_detection_start;
	public static String DetectorConsole_title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
