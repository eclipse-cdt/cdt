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
 * Red Hat Inc. - initial version
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher.ui.launchbar;

import org.eclipse.osgi.util.NLS;

/**
 * @since 1.2
 * @author jjohnstn
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.docker.launcher.ui.launchbar.messages"; //$NON-NLS-1$

	public static String ContainerGCCToolChainProvider_Saving1;
	public static String ContainerGCCToolChainProvider_Saving;
	public static String ContainerGCCToolChainProvider_NotOurs;
	public static String ContainerGCCToolChainProvider_Loading;

	public static String NewContainerTargetWizard_title;
	public static String NewContainerTargetWizardPage_name;
	public static String NewContainerTargetWizardPage_title;
	public static String NewContainerTargetWizardPage_description;
	public static String NewContainerTargetWizardPage_no_connections;
	public static String NewContainerTargetWizardPage_no_images;
	public static String NewContainerTargetWizardPage_connection;
	public static String NewContainerTargetWizardPage_image;

	public static String EditContainerTargetWizard_title;
	public static String EditContainerTargetWizardPage_title;
	public static String EditContainerTargetWizardPage_description;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
