/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.clangd;

import org.eclipse.osgi.util.NLS;

public class ClangdMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.lsp.internal.clangd.ClangdMessages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ClangdMessages.class);
	}

	public static String ClangdLanguageServer_label;

	private ClangdMessages() {
	}
}
