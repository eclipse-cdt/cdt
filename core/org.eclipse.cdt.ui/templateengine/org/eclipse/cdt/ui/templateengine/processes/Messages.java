/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.processes;

import org.eclipse.osgi.util.NLS;

/**
 * @since 5.2
 */
public class Messages {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.ui.templateengine.processes.messages"; //$NON-NLS-1$

	private Messages() {
	}

	public static String OpenFiles_CannotOpen_error;
	public static String OpenFiles_FileNotExist_error;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
