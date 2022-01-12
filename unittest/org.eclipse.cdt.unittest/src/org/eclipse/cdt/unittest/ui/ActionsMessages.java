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
package org.eclipse.cdt.unittest.ui;

import org.eclipse.osgi.util.NLS;

public class ActionsMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.unittest.ui.ActionsMessages"; //$NON-NLS-1$

	public static String OpenInEditorAction_text;
	public static String OpenInEditorAction_tooltip;
	public static String OpenEditorAction_action_label;
	public static String OpenEditorAction_error_cannotopen_title;
	public static String OpenEditorAction_error_cannotopen_message;
	public static String OpenEditorAction_errorOpenEditor;
	public static String OpenEditorAction_UpdateElementsJob_name;
	public static String OpenEditorAction_UpdateElementsJob_inProgress;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ActionsMessages.class);
	}

	private ActionsMessages() {
	}
}
