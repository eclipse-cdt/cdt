/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

/**
 * @since 1.1
 */
public class DsfDebugUITools {

	public static IPreferenceStore getPreferenceStore() {
		return DsfUIPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * @since 2.1
	 */
	public static void enableActivity(final String activityID, final boolean enableit) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
			IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
			IActivityManager activityManager = workbenchActivitySupport.getActivityManager();
			Set<String> enabledActivityIds = new HashSet<>(activityManager.getEnabledActivityIds());
			boolean changed = false;
			if (enableit)
				changed = enabledActivityIds.add(activityID);
			else
				changed = enabledActivityIds.remove(activityID);
			if (changed)
				workbenchActivitySupport.setEnabledActivityIds(enabledActivityIds);
		});
	}

}
