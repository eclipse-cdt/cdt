/*******************************************************************************
 * Copyright (c) 2010, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.actions;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle class for externalizing messages.
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.managedbuilder.internal.ui.actions.messages"; //$NON-NLS-1$
	public static String BuildConfigurationsJob_BuildError;
	public static String BuildConfigurationsJob_Building;
	public static String BuildConfigurationsJob_Cleaning;
	public static String CleanAndBuildDialog_Active;
	public static String CleanAndBuildDialog_BuildConfigurations;
	public static String CleanAndBuildDialog_CleanConfigurations;
	public static String CleanAndBuildDialog_RebuildConfigurations;
	public static String CleanAndBuildDialog_SelectConfigurations;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
