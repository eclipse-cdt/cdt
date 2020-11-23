/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.unittest.launcher;

import org.eclipse.osgi.util.NLS;

public class CDTMessages extends NLS {
	private static final String BUNDLE_NAME= "org.eclipse.cdt.unittest.launcher.CDTMessages"; //$NON-NLS-1$
	public static String TestingSession_finished_status;
	public static String TestingSession_name_format;
	public static String TestingSession_starting_status;
	public static String TestingSession_stopped_status;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CDTMessages.class);
	}

	private CDTMessages() {
	}
}
