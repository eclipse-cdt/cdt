/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.cmake.core.internal.messages"; //$NON-NLS-1$
	public static String CMakeBuildConfiguration_Building;
	public static String CMakeBuildConfiguration_BuildingIn;
	public static String CMakeBuildConfiguration_Cleaning;
	public static String CMakeBuildConfiguration_NotFound;
	public static String CMakeBuildConfiguration_ProcCompCmds;
	public static String CMakeBuildConfiguration_ProcCompJson;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
