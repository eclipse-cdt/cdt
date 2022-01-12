/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Patrick Hofer - Bug 326265
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.osgi.util.NLS;

public final class MakefileEditorMessages extends NLS {
	public static String MakefileEditor_menu_folding;
	public static String ToggleComment_error_title;
	public static String ToggleComment_error_message;

	static {
		NLS.initializeMessages(MakefileEditorMessages.class.getName(), MakefileEditorMessages.class);
	}

	// Do not instantiate
	private MakefileEditorMessages() {
	}
}