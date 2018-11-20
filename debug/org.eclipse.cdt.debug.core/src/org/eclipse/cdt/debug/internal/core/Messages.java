/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.internal.core.messages"; //$NON-NLS-1$
	public static String CoreBuildGenericLaunchConfigDelegate_CommandNotValid;
	public static String CoreBuildGenericLaunchConfigDelegate_NoAction;
	public static String CoreBuildGenericLaunchConfigDelegate_SubstitutionFailed;
	public static String CoreBuildGenericLaunchConfigDelegate_WorkingDirNotExists;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
