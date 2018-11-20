/*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.viewsupport.messages"; //$NON-NLS-1$

	public static String EditorOpener_fileDoesNotExist;
	public static String IndexedFilesCache_jobName;
	public static String IndexUI_infoNotInIndex;
	public static String IndexUI_infoNotInSource;
	public static String IndexUI_infoSelectIndexAllFiles;
	public static String SelectionListenerWithASTManager_jobName;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
