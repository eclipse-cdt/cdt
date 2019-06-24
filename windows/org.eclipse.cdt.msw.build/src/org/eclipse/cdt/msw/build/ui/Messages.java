/*******************************************************************************
 * Copyright (c) 2019 Marc-Andre Laperle and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.msw.build.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String MSVCCommandParserOptionPage_CompilerPattern;
	public static String MSVCCommandParserOptionPage_ContainerForDiscoveredEntries;
	public static String MSVCCommandParserOptionPage_ResolvePaths;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
