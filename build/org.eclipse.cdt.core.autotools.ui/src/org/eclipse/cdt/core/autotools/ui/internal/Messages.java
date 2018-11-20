/*******************************************************************************
 * Copyright (c) 2017 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.autotools.ui.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.core.autotools.ui.internal.messages"; //$NON-NLS-1$
	public static String NewAutotoolsProjectWizard_Description;
	public static String NewAutotoolsProjectWizard_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
