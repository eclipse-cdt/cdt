/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.opentype;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class OpenTypeMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.browser.opentype.OpenTypeMessages";//$NON-NLS-1$

	private OpenTypeMessages() {
		// Do not instantiate
	}

	public static String OpenTypeAction_errorTitle;
	public static String OpenTypeAction_errorOpenEditor;
	public static String OpenTypeAction_errorTypeNotFound;
	public static String OpenTypeDialog_title;
	public static String OpenTypeDialog_message;
	public static String ElementSelectionDialog_UpdateElementsJob_name;
	public static String ElementSelectionDialog_UpdateElementsJob_inProgress;

	static {
		NLS.initializeMessages(BUNDLE_NAME, OpenTypeMessages.class);
	}
}