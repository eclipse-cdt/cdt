/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.gnu.tools;

import org.eclipse.cdt.core.builder.model.ICBuildConfigProvider;
import org.eclipse.cdt.core.builder.model.ICBuildConfigWorkingCopy;
import org.eclipse.cdt.core.builder.model.ICPosixBuildConstants;

/**
 * Default implementation of a "Debug" build configuration
 * provider for GNU toolchains.
 */
public class CConfigProviderDebug implements ICBuildConfigProvider {

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICBuildConfigProvider#setDefaults(ICBuildConfigWorkingCopy)
	 */
	public void setDefaults(ICBuildConfigWorkingCopy config) {
		config.setAttribute(ICPosixBuildConstants.CC_ENABLE_DEBUG, true);
		config.setAttribute(ICPosixBuildConstants.CC_WARN_ALL, true);
		config.setAttribute(ICPosixBuildConstants.LD_STRIP, false);
		config.setAttribute(ICPosixBuildConstants.CC_ENABLE_PROFILE, false);
		config.setAttribute(ICPosixBuildConstants.CC_ENABLE_OPTIMIZE, false);
		config.setAttribute(ICPosixBuildConstants.CC_OPTIMZE_LEVEL,
							ICPosixBuildConstants.CC_OPTIMIZE_NONE);
	}

}
