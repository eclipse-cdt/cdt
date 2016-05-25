/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Jonah Graham (Kichwa Coders) - Bug 494504: Resurrect to ease transition for extenders from CDT 8.x to 9.x
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @deprecated Use GdbDebugServicesFactory directly, passing in the launch configuration to the
 * constructor from which the Non-stop is derived.
 */
@Deprecated
public class GdbDebugServicesFactoryNS extends GdbDebugServicesFactory {

	public GdbDebugServicesFactoryNS(String version) {
		super(version, null);
	}

	public GdbDebugServicesFactoryNS(String version, ILaunchConfiguration configuration) {
		super(version, configuration);
	}

	/**
	 * Return true if configuration is null.
	 */
	@Override
	protected boolean getIsNonStopMode() {
		ILaunchConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return true;
		}
		return LaunchUtils.getIsNonStopMode(configuration);
	}

}
