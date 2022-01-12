/*******************************************************************************
 * Copyright (c) 2021 Mat Booth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tools.templates.ui;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {

	public static String ProjectImportConfigurator_Checking;

	public static String TemplateWizard_CannotBeCreated;
	public static String TemplateWizard_ErrorCreating;
	public static String TemplateWizard_FailedToOpen;
	public static String TemplateWizard_Generating;
	public static String TemplateWizard_InternalError;

	static {
		NLS.initializeMessages(Messages.class.getPackageName() + ".messages", Messages.class); //$NON-NLS-1$
	}

	private Messages() {
	}
}
