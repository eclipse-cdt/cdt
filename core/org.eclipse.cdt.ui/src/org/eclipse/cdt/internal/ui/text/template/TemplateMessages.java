/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.template;

import org.eclipse.osgi.util.NLS;

public final class TemplateMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.text.template.TemplateMessages";//$NON-NLS-1$

	private TemplateMessages() {
		// Do not instantiate
	}

	public static String TemplateVariableProposal_error_title;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TemplateMessages.class);
	}
}
