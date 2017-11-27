/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.build;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.core.build.messages"; //$NON-NLS-1$
	public static String CBuildConfiguration_CreateJob;
	public static String CBuildConfiguration_ToolchainMissing;
	public static String CBuildConfiguration_Location;
	public static String CBuilder_ExceptionWhileBuilding;
	public static String CBuilder_ExceptionWhileBuilding2;
	public static String CBuilder_NotConfiguredCorrectly;
	public static String CBuilder_NotConfiguredCorrectly2;
	public static String StandardBuildConfiguration_0;
	public static String StandardBuildConfiguration_1;
	public static String StandardBuildConfiguration_CommandNotFound;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
