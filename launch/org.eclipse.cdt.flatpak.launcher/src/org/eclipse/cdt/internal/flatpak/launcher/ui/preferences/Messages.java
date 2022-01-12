/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.flatpak.launcher.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.flatpak.launcher.ui.preferences.messages"; //$NON-NLS-1$

	public static String FlatpakDebugProcess_label;
	public static String FlatpakRemoveHeaders_label;
	public static String FlatpakRemoveHeaders_tooltip;
	public static String FlatpakConfirmRemoval_title;
	public static String FlatpakConfirmRemoval_msg;
	public static String FlatpakDirectories_label;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
