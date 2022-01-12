/*******************************************************************************
 * Copyright (c) 2019, 2020 Eclipse contributors and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.cquery;

import org.eclipse.osgi.util.NLS;

public class CqueryMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.lsp.internal.cquery.CqueryMessages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CqueryMessages.class);
	}

	public static String CqueryLanguageServer_label;
	public static String CquerySymbolKind_e_illegal_value;
	public static String StorageClass_e_illegal_value;

	private CqueryMessages() {
	}
}
