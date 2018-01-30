/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Red Hat Inc. - initial version
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.meson.core.messages"; //$NON-NLS-1$
	
	public static String MesonBuildConfiguration_Building;
	public static String MesonBuildConfiguration_BuildingIn;
	public static String MesonBuildConfiguration_BuildingComplete;
	public static String MesonBuildConfiguration_Cleaning;
	public static String MesonBuildConfiguration_RunningMeson;
	public static String MesonBuildConfiguration_RunningMesonFailure;
	public static String MesonBuildConfiguration_RunningNinjaFailure;
	public static String MesonBuildConfiguration_NoToolchainFile;
	public static String MesonBuildConfiguration_ProcCompCmds;
	public static String MesonBuildConfiguration_ProcCompJson;

	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

