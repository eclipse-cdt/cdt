/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Elazar Leibovich (IDF) - Code folding of compound statements (bug 174597)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.folding;

import org.eclipse.osgi.util.NLS;

public final class FoldingMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.text.folding.FoldingMessages";//$NON-NLS-1$

	private FoldingMessages() {
		// Do not instantiate
	}

	public static String DefaultCFoldingPreferenceBlock_title;
	public static String DefaultCFoldingPreferenceBlock_macros;
	public static String DefaultCFoldingPreferenceBlock_functions;
	public static String DefaultCFoldingPreferenceBlock_methods;
	public static String DefaultCFoldingPreferenceBlock_structures;
	public static String DefaultCFoldingPreferenceBlock_comments;
	public static String DefaultCFoldingPreferenceBlock_doc_comments;
	public static String DefaultCFoldingPreferenceBlock_non_doc_comments;
	public static String DefaultCFoldingPreferenceBlock_headers;
	public static String DefaultCFoldingPreferenceBlock_inactive_code;
	public static String DefaultCFoldingPreferenceBlock_preprocessor_enabled;
	public static String EmptyCFoldingPreferenceBlock_emptyCaption;
	public static String DefaultCFoldingPreferenceBlock_statements_enabled;

	static {
		NLS.initializeMessages(BUNDLE_NAME, FoldingMessages.class);
	}
}
