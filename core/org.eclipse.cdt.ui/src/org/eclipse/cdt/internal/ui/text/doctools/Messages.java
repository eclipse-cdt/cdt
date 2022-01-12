/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.doctools;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.text.doctools.messages"; //$NON-NLS-1$
	public static String DocCommentOwnerManager_DuplicateMapping0;
	public static String EditorReopener_ReopenJobComplete;
	public static String EditorReopener_ReopenJobStart;
	public static String EditorReopener_ShouldSave_Message;
	public static String EditorReopener_ShouldSave_Title;
	public static String NullDocCommentOwner_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
