/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui.wizards;

import org.eclipse.osgi.util.NLS;

public final class WizardMessages extends NLS {

	public static String RunNinjaPage_name;
	public static String RunNinjaPage_description;
	public static String RunNinjaPage_title;
	public static String RunNinjaPage_env_label;
	public static String RunNinjaPage_env_description;
	public static String RunNinjaPage_options_label;
	public static String RunNinjaPage_options_description;

	static {
		// initialize resource bundle
		NLS.initializeMessages("org.eclipse.cdt.internal.meson.ui.wizards.wizardmessages", WizardMessages.class); //$NON-NLS-1$
	}

	private WizardMessages() {
	}
}
