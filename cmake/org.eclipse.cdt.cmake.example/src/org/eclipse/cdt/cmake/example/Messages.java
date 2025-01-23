/*******************************************************************************
 * Copyright (c) 2017, 2025 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.example;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String NewExtendedCMakeProjectWizard_Description;
	public static String NewExtendedCMakeProjectWizard_PageTitle;
	public static String NewExtendedCMakeProjectWizard_WindowTitle;

	static {
		// initialize resource bundle
		NLS.initializeMessages("org.eclipse.cdt.cmake.example.messages", Messages.class); //$NON-NLS-1$
	}

	private Messages() {
	}
}
