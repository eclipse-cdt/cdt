/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.local.controls;

import org.eclipse.osgi.util.NLS;

/**
 * @noreference This class is not intended to be referenced by clients.
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.tm.terminal.connector.local.controls.Messages"; //$NON-NLS-1$
	public static String LocalWizardConfigurationPanel_encoding_does_not_match;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
