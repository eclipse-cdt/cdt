/*******************************************************************************
 * Copyright (c) 2019 Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - initial API and implementatin
 *******************************************************************************/
package org.eclipse.cdt.internal.cquery;

import org.eclipse.osgi.util.NLS;

public class CqueryMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.cquery.CqueryMessages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CqueryMessages.class);
	}

	public static String CquerySymbolKind_e_illegal_value;
	public static String Server2ClientProtocolExtension_cquery_busy;
	public static String Server2ClientProtocolExtension_cquery_idle;
	public static String StorageClass_e_illegal_value;

	private CqueryMessages() {
	}
}
