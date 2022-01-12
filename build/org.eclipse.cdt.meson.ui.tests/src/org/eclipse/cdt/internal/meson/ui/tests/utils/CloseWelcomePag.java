/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.cdt.internal.meson.ui.tests.utils;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Closes the Welcome page and optionally opens a given perspective
 */
public class CloseWelcomePag implements BeforeEachCallback {

	public static final String CDT_PERSPECTIVE_ID = "org.eclipse.cdt.ui.CPerspective";

	/** the Id of the perspective to open. */
	private final String defaultPerspectiveId;

	public CloseWelcomePag() {
		this.defaultPerspectiveId = CloseWelcomePag.CDT_PERSPECTIVE_ID;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		Display.getDefault().syncExec(() -> {
			final IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench.getIntroManager().getIntro() != null) {
				workbench.getIntroManager().closeIntro(workbench.getIntroManager().getIntro());
			}
			try {
				workbench.showPerspective(defaultPerspectiveId, workbench.getActiveWorkbenchWindow());
			} catch (WorkbenchException e) {
				e.printStackTrace();
			}
		});
		final String PREF_ENABLE_LAUNCHBAR = "enableLaunchBar"; //$NON-NLS-1$
		final String PREF_ENABLE_TARGETSELECTOR = "enableTargetSelector"; //$NON-NLS-1$
		final String PREF_ENABLE_BUILDBUTTON = "enableBuildButton"; //$NON-NLS-1$

		Display.getDefault().asyncExec(() -> {
			final IPreferenceStore store = org.eclipse.launchbar.ui.controls.internal.Activator.getDefault()
					.getPreferenceStore();
			store.setValue(PREF_ENABLE_LAUNCHBAR, false);
			store.setValue(PREF_ENABLE_TARGETSELECTOR, false);
			store.setValue(PREF_ENABLE_BUILDBUTTON, false);
		});
	}

}
