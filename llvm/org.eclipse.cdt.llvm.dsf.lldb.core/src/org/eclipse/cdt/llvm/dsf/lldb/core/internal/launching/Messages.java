/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.launching;

import org.eclipse.osgi.util.NLS;

/**
 * Messages related to launching LLDB.
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.llvm.dsf.lldb.core.internal.launching.messages"; //$NON-NLS-1$
	public static String LLDBLaunch_minimum_version_error;
	public static String LLDBLaunchDelegate_mimicking_gdb;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
