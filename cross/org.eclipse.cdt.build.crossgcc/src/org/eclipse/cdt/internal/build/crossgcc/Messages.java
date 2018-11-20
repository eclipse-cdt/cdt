/*******************************************************************************
 * Copyright (c) 2011, 2014 Marc-Andre Laperle and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.build.crossgcc;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String SetCrossCommandWizardPage_browse;
	public static String SetCrossCommandWizardPage_description;
	public static String SetCrossCommandWizardPage_name;
	public static String SetCrossCommandWizardPage_path;
	public static String SetCrossCommandWizardPage_prefix;
	public static String SetCrossCommandWizardPage_title;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
