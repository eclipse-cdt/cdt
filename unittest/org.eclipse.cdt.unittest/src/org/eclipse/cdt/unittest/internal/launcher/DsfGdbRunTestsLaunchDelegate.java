/*******************************************************************************
 * Copyright (c) 2011, 2020 Anton Gorenkov.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.unittest.internal.launcher;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.unittest.CDTUnitTestPlugin;
import org.eclipse.cdt.unittest.launcher.BaseTestsLaunchDelegate;

/**
 * Launch delegate implementation that redirects its queries to DSF.
 */
public class DsfGdbRunTestsLaunchDelegate extends BaseTestsLaunchDelegate {

	@Override
	public String getPreferredDelegateId() {
		return ICDTLaunchConfigurationConstants.PREFERRED_DEBUG_LOCAL_LAUNCH_DELEGATE;
	}

	@Override
	public String getUnitTestViewSupportID() {
		return CDTUnitTestPlugin.CDT_TEST_VIEW_SUPPORT_ID;
	}

}
